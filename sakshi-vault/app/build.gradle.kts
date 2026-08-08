plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "rajnishkmehta.sakshi.vault"
    compileSdk = 37

    defaultConfig {
        applicationId = "rajnishkmehta.sakshi.vault"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = versionCode.toString()
    }

    buildFeatures {
        buildConfig = true
    }
/*
    // ABI Splits Configuration
    }
    splits {
        abi {
            isEnable = isReleaseBuild
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
*/
    // Define the keystore file based on the environment variable or default to "release.keystore"
    val keystorePath = System.getenv("KEYSTORE_FILE") ?: "release.keystore"
    val keystore = file(keystorePath)

    signingConfigs {
        // only configure the release signing if the keystore exists
        if (keystore.exists()) {
            create("release") {
                storeFile = keystore
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Apply the signing config
            if (keystore.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-dev"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

tasks.configureEach {
    if (name.contains("AarMetadata")) {
        enabled = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)

    // Sakshi SDK
    implementation(libs.sakshi.sdk)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
