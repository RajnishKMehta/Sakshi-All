plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    buildToolsVersion = "37.0.0"
    namespace = "rajnishkmehta.sakshi.vault"
    compileSdk = 37

    defaultConfig {
        applicationId = "rajnishkmehta.sakshi.vault"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = versionCode.toString() + "-beta"
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

    // Room
    val room_version = "3.0.1"
    implementation("androidx.room3:room3-runtime:$room_version")

    ksp("androidx.room3:room3-compiler:$room_version")


    // Sakshi SDK
    // local
    implementation(project(":sakshi-sdk"))
    // Maven Central
    //implementation(libs.sakshi.sdk)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}
