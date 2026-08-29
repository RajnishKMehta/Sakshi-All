import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

android {
    buildToolsVersion = "37.0.0"
    namespace = "rajnishkmehta.sakshi.sdk"
    compileSdk = 37

    defaultConfig {
        minSdk = 29

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        aidl = true
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xexplicit-api=strict")
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
}

mavenPublishing {
    coordinates("io.github.rajnishkmehta.sakshi", "sakshi-sdk", libs.versions.sakshi.sdk.get())

    publishToMavenCentral(automaticRelease = true)
    // signAllPublications()

    configure(AndroidSingleVariantLibrary(
        javadocJar = JavadocJar.Empty(),
        sourcesJar = SourcesJar.Sources(),
        variant = "release"
    ))

    pom {
        name.set("Sakshi SDK")
        description.set("Official Android SDK for building secure integrations with the Sakshi ecosystem through a modern IPC communication layer.")
        url.set("https://github.com/RajnishKMehta/sakshi-sdk")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("rajnishkmehta")
                name.set("Rajnish")
                email.set("RajnishKMehta@proton.me")
            }
        }

        scm {
            connection.set("scm:git:github.com/RajnishKMehta/sakshi-sdk.git")
            developerConnection.set("scm:git:ssh://github.com/RajnishKMehta/sakshi-sdk.git")
            url.set("https://github.com/RajnishKMehta/sakshi-sdk")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/RajnishKMehta/sakshi-sdk")
            credentials(PasswordCredentials::class)
        }
    }
}
