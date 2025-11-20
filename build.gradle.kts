// build.gradle.kts (Project: S16Group7ITISMOB)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // CRITICAL ADDITION: Defines the Google Services plugin version
    id("com.google.gms.google-services") version "4.4.2" apply false
}