/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.jvm.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirQualifiedAccessChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.jvm.FirJvmErrors
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccess
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

object FirEnumDeclaringClassDeprecationChecker : FirQualifiedAccessChecker() {
    override fun check(expression: FirQualifiedAccess, context: CheckerContext, reporter: DiagnosticReporter) {
        val calleeReference = expression.calleeReference as? FirResolvedNamedReference ?: return
        val resolvedSymbol = calleeReference.resolvedSymbol as? FirCallableSymbol ?: return
        if (resolvedSymbol.callableId == GET_DECLARING_CLASS_ID || resolvedSymbol.callableId == DECLARING_CLASS_ID) {
            reporter.reportOn(
                expression.source,
                FirJvmErrors.ENUM_DECLARING_CLASS_DEPRECATED,
                context,
            )
        }
    }

    private val GET_DECLARING_CLASS_ID = CallableId(StandardClassIds.Enum, Name.identifier("getDeclaringClass"))
    private val DECLARING_CLASS_ID = CallableId(StandardClassIds.Enum, Name.identifier("declaringClass"))
}