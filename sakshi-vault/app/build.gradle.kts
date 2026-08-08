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
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)

    // Sakshi SDK
    implementation(project(":sakshi-sdk"))

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.coroutines.android)
}
