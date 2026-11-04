---
description: "Integrate the Sākṣī SDK into your Android application to communicate with the Sākṣī Vault service."
icon: puzzle-piece
---
# SDK Integration

Integrate the Sākṣī SDK into your Android application to communicate with the Sākṣī Vault service.

## Requirements

- **Android:** API 29 (Android 10) or higher.
- **Language:** Kotlin.
- **Coroutines:** Kotlin coroutines are used for asynchronous SDK operations.

## Add the SDK Dependency

The SDK is published to two Maven repositories:

1. **GitHub Packages:** Primary distribution.
2. **Maven Central:** Secondary distribution.

Choose the repository you want to use and configure it in `settings.gradle.kts`. The dependency coordinates are identical for both repositories.

### GitHub Packages (Primary)

[![GitHub Release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-SDK?include_prereleases&sort=semver&style=flat-square&logo=github&logoColor=white&label=GitHub%20Release)](https://github.com/RajnishKMehta/Sakshi-SDK/releases)

Add GitHub Packages before Maven Central in `settings.gradle.kts`. This makes GitHub Packages the first repository Gradle checks for the SDK, with Maven Central available as a fallback.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/RajnishKMehta/Sakshi-SDK")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }

        mavenCentral()
        google()
    }
}
```

**Authentication**

GitHub Packages requires authentication to download packages, even when the package is public. Create a **Personal Access Token (classic)** with the `read:packages` scope.

For local development, add your credentials to your user-level Gradle properties file:

```properties
# ~/.gradle/gradle.properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

Keep this token out of your project files and source control. In GitHub Actions, use the built-in `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables, and grant the workflow `packages: read` permission. The workflow must also have access to the package.

### Maven Central (Secondary)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.rajnishkmehta.sakshi/sakshi-sdk?style=flat-square&logo=apachemaven&logoColor=white&label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.rajnishkmehta.sakshi/sakshi-sdk)

Maven Central is public and does not require credentials. If you want to use Maven Central instead of GitHub Packages, configure it in `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
```

If you configure both repositories, keep GitHub Packages before Maven Central as shown in the primary configuration above.

### Declare the Dependency

Use a Gradle version catalog so the SDK version is declared in one place rather than repeated in every module.

Add the version and library alias to `gradle/libs.versions.toml`. Set the version to the release shown by the badge for the repository you selected.

```toml
[versions]
sakshi-sdk = "VERSION_FROM_THE_BADGE"

[libraries]
sakshi-sdk = { module = "io.github.rajnishkmehta.sakshi:sakshi-sdk", version.ref = "sakshi-sdk" }
```

Then add the dependency to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.sakshi.sdk)
}
```

The version is now maintained in one place: `gradle/libs.versions.toml`. When upgrading the SDK, update that single version entry. The GitHub Release and Maven Central badges independently show the versions published to their respective destinations; check that the selected repository has the version you want before updating the catalog.

## Manifest Configuration

To discover and communicate with the default Vault service, declare the package and service action in your app's `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <queries>
        <package android:name="rajnishkmehta.sakshi.vault" />
        <intent>
            <action android:name="rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE" />
        </intent>
    </queries>

</manifest>
```

## Initialize the SDK

The SDK entry point is the [`SakshiClient`](../../developer-reference/api/README.md) interface. Create an instance with an application or component `Context`.

### Default Configuration

The default configuration connects to the standard Vault package and service action.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

// Prefer applicationContext to avoid retaining an Activity.
val sakshiClient = SakshiClient.create(applicationContext)
```

### Custom Configuration

Provide a [`SakshiClientConfig`](../../developer-reference/configuration.md) when connecting to a Vault installation with a different package name or service action, or when changing the connection timeout.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.SakshiClientConfig

val config = SakshiClientConfig(
    vaultPackageName = "com.example.customvault",
    vaultServiceAction = "com.example.customvault.BIND_VAULT_SERVICE",
    connectionTimeoutMs = 10_000L
)

val sakshiClient = SakshiClient.create(applicationContext, config)
```

## Disconnect and Release Resources

The SDK manages service binding while API calls are in progress. Explicitly disconnect when the component or owner no longer needs the client.

For example, in a `ViewModel`:

```kotlin
override fun onCleared() {
    super.onCleared()
    sakshiClient.disconnect()
}
```
