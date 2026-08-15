// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  dependencies {
    // Pins the Compose compiler Gradle plugin artifact explicitly (per Android's current
    // Compose setup docs) so its version can't drift from what's declared below in
    // gradle/libs.versions.toml (kotlin = "2.3.21") - this is what was ambiguous and caused
    // the "ComposePluginRegistrar is incompatible with the current version of the compiler"
    // AbstractMethodError: relying only on the plugins{} DSL block left room for AGP's own
    // auto-supplied Compose compiler coordinate to resolve a different, incompatible version.
    classpath("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.3.21")
  }
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.roborazzi) apply false
}
