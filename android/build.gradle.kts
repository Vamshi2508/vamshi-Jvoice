plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    // Declared but not applied here. The app module applies it CONDITIONALLY -
    // see app/build.gradle.kts - because the plugin hard-fails the build when
    // google-services.json is absent, and this project has to keep building
    // before the Firebase project exists.
    id("com.google.gms.google-services") version "4.4.2" apply false
}
