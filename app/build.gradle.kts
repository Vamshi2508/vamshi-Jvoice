plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

/**
 * Is a real Firebase config present?
 *
 * The google-services plugin fails the build outright when google-services.json
 * is missing, which would mean nobody can compile this project until the Firebase
 * project exists. So the plugin is applied only when the file is actually there,
 * and the app checks the same condition at runtime (see FirebaseAvailability) to
 * decide between real Firebase auth and the local demo logins.
 *
 * Consequence worth knowing: dropping google-services.json in is the ONLY step
 * needed to switch the app over. No code or Gradle edit.
 */
val googleServicesFile = file("google-services.json")
val hasFirebaseConfig = googleServicesFile.exists()

if (hasFirebaseConfig) {
    apply(plugin = "com.google.gms.google-services")
    logger.lifecycle("J Voice: google-services.json found - Firebase is wired in.")
} else {
    logger.lifecycle(
        "J Voice: no google-services.json - building WITHOUT Firebase. " +
            "The app will run on local demo logins. See firebase/README.md."
    )
}

android {
    namespace = "com.jvoice.news"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jvoice.news"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0-module1-prototype"
        vectorDrawables { useSupportLibrary = true }

        // Mirrors the Gradle-time check above into the generated BuildConfig, so
        // the runtime guard and the build can never disagree about whether
        // Firebase is present.
        buildConfigField("boolean", "HAS_FIREBASE_CONFIG", hasFirebaseConfig.toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }

    // The study-content export (SeedExportTest) writes its JSON next to the
    // uploader that consumes it, in the sibling `firebase` directory. Passed as a
    // property rather than assumed from the test's working directory, which AGP
    // does not guarantee.
    testOptions {
        unitTests.all {
            it.systemProperty(
                "seed.out",
                rootProject.file("../firebase/jvoice-seed.json").absolutePath
            )
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ------------------------------------------------------------------ Firebase
    // The BOM pins every Firebase artifact to one compatible set, so individual
    // dependencies below carry no version.
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    // Auth: email/password sign-in.
    implementation("com.google.firebase:firebase-auth")
    // Realtime Database: the users/ tree and the session signals that hang off it
    // (role, isLogin, forceLogoutAt, update). Chosen for auth because the
    // kill-switch and force-logout guards are built on RTDB value events.
    implementation("com.google.firebase:firebase-database")
    // Firestore: the news and study content, which needs real queries. The News
    // module reads and writes it; the Study module is still on bundled data.
    implementation("com.google.firebase:firebase-firestore")
    // Coroutine adapters for Task<T>, so the auth layer can `await()` instead of
    // nesting completion listeners.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Unit tests only: the one-off study-content export. No Android or Robolectric
    // dependency, because the data and codecs it walks are plain Kotlin.
    testImplementation("junit:junit:4.13.2")
}
