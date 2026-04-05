package dev.parcelview.buildplugins

import org.jetbrains.amper.plugins.Configurable

@Configurable
interface BuildInfoSettings {
    val name: String get() = "ParcelView Backend"

    val version: String get() = "0.0.1"
}