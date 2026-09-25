/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    compileSdk = 37
    compileSdkMinor = 2
    ndkVersion = "30.0.16248370"
    namespace = "rajnishkmehta.sakshi.vault"

    defaultConfig {
        applicationId = "rajnishkmehta.sakshi.vault"
        minSdk = 29
        targetSdk = 37
        versionCode = 2
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

    testOptions {
        unitTests.isReturnDefaultValues = true
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
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)


    // Sakshi SDK
    implementation(libs.sakshi.sdk)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.guava)

    // Media3
    implementation(libs.androidx.media3.inspector.frame)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
