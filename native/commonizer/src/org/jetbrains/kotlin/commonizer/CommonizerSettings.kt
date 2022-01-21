/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

/**
 * Optional configuration settings for commonization task
 */
interface CommonizerSettings {

    sealed class Key<T : Any> {
        abstract val defaultValue: T
    }

    fun <T : Any> getSetting(key: Key<T>): T
}

object OptimisticNumberCommonizationEnabled : CommonizerSettings.Key<Boolean>() {
    override val defaultValue: Boolean = true
}

internal object DefaultCommonizerSettings : CommonizerSettings {
    override fun <T : Any> getSetting(key: CommonizerSettings.Key<T>): T {
        return key.defaultValue
    }
}

internal class MapBasedCommonizerSettings(
    vararg settings: CommonizerSetting<*>
) : CommonizerSettings {
    private val settings: Map<CommonizerSettings.Key<*>, Any> = settings.associate { (k, v) -> k to v }

    override fun <T : Any> getSetting(key: CommonizerSettings.Key<T>): T {
        @Suppress("UNCHECKED_CAST")
        return settings[key] as? T ?: key.defaultValue
    }
}

internal data class CommonizerSetting<T : Any>(
    internal val key: CommonizerSettings.Key<T>,
    internal val settingValue: T,
)
