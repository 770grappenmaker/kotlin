/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.dce

import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.js.dce.UselessDeclarationsRemover
import org.jetbrains.kotlin.ir.backend.js.utils.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.js.config.JSConfigurationKeys

internal fun eliminateDeadDeclarations(modules: List<IrModuleFragment>, context: WasmBackendContext) {
    val printReachabilityInfo =
        context.configuration.getBoolean(JSConfigurationKeys.PRINT_REACHABILITY_INFO) ||
                java.lang.Boolean.getBoolean("kotlin.wasm.dce.print.reachability.info")

    val usefulDeclarations = WasmUsefulDeclarationProcessor(
        context = context,
        printReachabilityInfo = printReachabilityInfo
    ).collectDeclarations(rootDeclarations = buildRoots(modules, context))

    removeUnusedDeclarations(
        context = context,
        modules = modules,
        usefulDeclarations = usefulDeclarations
    )
}

private fun removeUnusedDeclarations(
    context: WasmBackendContext,
    modules: List<IrModuleFragment>,
    usefulDeclarations: Set<IrDeclaration>
) {
    val remover = UselessDeclarationsRemover(
        removeUnusedAssociatedObjects = false,
        usefulDeclarations = usefulDeclarations,
        context = context,
        dceRuntimeDiagnostic = null,
    )

    modules.onAllFiles {
        acceptVoid(remover)
    }
}

private fun buildRoots(modules: List<IrModuleFragment>, context: WasmBackendContext): List<IrDeclaration> = buildList {
    val nestedDeclarationsVisitor = object : IrElementVisitorVoid {
        override fun visitElement(element: IrElement): Unit = element.acceptChildrenVoid(this)
        override fun visitBody(body: IrBody): Unit = Unit // Skip

        override fun visitDeclaration(declaration: IrDeclarationBase) {
            super.visitDeclaration(declaration)
            add(declaration)
        }
    }

    modules.onAllFiles {
        declarations.forEach { declaration ->
            if (declaration.isJsExport()) {
                declaration.acceptVoid(nestedDeclarationsVisitor)
            }
        }
    }

    add(context.irBuiltIns.throwableClass.owner)
    add(context.mainCallsWrapperFunction)
    add(context.fieldInitFunction)
}

private inline fun List<IrModuleFragment>.onAllFiles(body: IrFile.() -> Unit) {
    forEach { module ->
        module.files.forEach { file ->
            file.body()
        }
    }
}