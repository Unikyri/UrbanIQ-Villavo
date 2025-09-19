// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Plugins por defecto de Android Studio (usando alias)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Plugins que añadimos (usando id)
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.dagger.hilt.android") version "2.57.1" apply false
}