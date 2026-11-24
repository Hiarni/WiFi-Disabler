plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "dev.hiarni.wifidisabler"
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        applicationId = "dev.hiarni.wifidisabler"
        minSdk = 26
        // Deliberately kept at 28: since Android 10, WifiManager.setWifiEnabled() only works for
        // apps targeting API 28 or lower. This is the core mechanism of the app – see README.
        //noinspection ExpiredTargetSdkVersion,OldTargetApi
        targetSdk = 28
        versionCode = 2
        versionName = "1.1.0"
    }

    // Signatur
    signingConfigs {
        create("wifidisabler") {
            storeFile = file("wifidisabler.keystore")
            storePassword = "wifidisabler"
            keyAlias = "wifidisabler"
            keyPassword = "wifidisabler"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("wifidisabler")
        }
        debug {
            signingConfig = signingConfigs.getByName("wifidisabler")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    androidResources {
        localeFilters += setOf("de", "en")
    }

    dependenciesInfo {
        // No Google-encrypted dependency blob in the APK (keeps builds transparent / F-Droid friendly).
        includeInApk = false
        includeInBundle = false
    }

    lint {
        // targetSdk 28 is intentional (see above).
        disable += setOf("ExpiredTargetSdkVersion", "OldTargetApi")
        // mipmap-anydpi-v26 is still required by aapt for adaptive icons.
        disable += "ObsoleteSdkInt"
        abortOnError = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "DebugProbesKt.bin")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
