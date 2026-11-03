# SDK Integration

Integrating the Sākṣī SDK allows your Android application to securely transmit media to or query media from the Vault application.

[![GitHub Packages](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-SDK?include_prereleases&sort=semver&display_name=tag&style=flat-square&logo=github&logoColor=white&label=GitHub%20Packages)](https://github.com/RajnishKMehta/Sakshi-SDK/packages/3153108)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.rajnishkmehta.sakshi/sakshi-sdk?style=flat-square&logo=apachemaven&logoColor=white&label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.rajnishkmehta.sakshi/sakshi-sdk)

The GitHub badge follows the latest GitHub Release tag, including pre-releases. The SDK release workflow publishes the same version to GitHub Packages and Maven Central before creating the corresponding GitHub Release.

## Minimum Requirements

*   **Android API Level**: The Sākṣī SDK and all its components require a minimum SDK version of API 29 (Android 10).
*   **Kotlin Coroutines**: The SDK relies entirely on Kotlin coroutines for asynchronous operations.

## Dependency Configuration

The Sākṣī SDK is available from two Maven repositories. **GitHub Packages is the primary distribution**, with Maven Central available as a secondary repository.

### 1. GitHub Packages (Primary)

Add the GitHub Packages repository to your dependency resolution configuration in `settings.gradle.kts`. Keep it before Maven Central so Gradle checks GitHub Packages first.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/RajnishKMehta/Sakshi-SDK")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrNull()
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").getOrNull()
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
        mavenCentral()
    }
}
```

GitHub Packages requires authentication to download packages, including public packages. Create a **Personal Access Token (classic)** with the `read:packages` scope.

For local development, add your GitHub username and token to your user-level Gradle properties file:

```properties
# ~/.gradle/gradle.properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

Do not put the token in the project-level `gradle.properties` file or commit it to the repository. In GitHub Actions, you can use `GITHUB_ACTOR` and `GITHUB_TOKEN`; ensure the workflow repository has been granted access to the package.

Add the SDK dependency to your app module's `build.gradle.kts`:

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    implementation("io.github.rajnishkmehta.sakshi:sakshi-sdk:1.0.0-RC.4")
}
```

Use an explicit version rather than a dynamic `latest` or `+` version so builds remain reproducible. The GitHub Packages badge above shows the current release version.

### 2. Maven Central (Secondary)

Maven Central is a public repository and does not require GitHub credentials. If you want to consume the SDK from Maven Central only, configure it in `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

The dependency coordinates are the same:

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    implementation("io.github.rajnishkmehta.sakshi:sakshi-sdk:1.0.0-RC.4")
}
```

The Maven Central badge above independently shows the latest version available from Maven Central. You can compare it with the GitHub Packages version before choosing a release.

## Required Permissions and Manifest Setup

To communicate with the Vault application via IPC, your client application must declare the appropriate queries and permissions in its `AndroidManifest.xml`.

The standard configuration targets the default Vault package name (`rajnishkmehta.sakshi.vault`).

```xml
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Required to discover the Vault IPC service -->
    <queries>
        <package android:name="rajnishkmehta.sakshi.vault" />
        <intent>
            <action android:name="rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE" />
        </intent>
    </queries>

</manifest>
```

## Initialization and Configuration

The entry point to the SDK is the [`SakshiClient`](../../developer-reference/api/README.md) interface. You instantiate it using [`SakshiClient.create()`](../../developer-reference/api/README.md#create), passing in an application or component `Context` and an optional [`SakshiClientConfig`](../../developer-reference/configuration.md).

### Default Configuration

By default, the SDK is configured to connect to the standard Vault package and intent action.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

// Using the application context is recommended to prevent memory leaks
val sakshiClient = SakshiClient.create(applicationContext)
```

### Custom Configuration

If you need to connect to a different package name or modify the connection timeout, you can provide a custom [`SakshiClientConfig`](../../developer-reference/configuration.md).

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.SakshiClientConfig

val config = SakshiClientConfig(
    vaultPackageName = "com.example.customvault",
    vaultServiceAction = "com.example.customvault.BIND_VAULT_SERVICE",
    connectionTimeoutMs = 10000L // 10 seconds
)

val sakshiClient = SakshiClient.create(applicationContext, config)
```

## Connection and Lifecycle Handling

The [`SakshiClient`](../../developer-reference/api/README.md) automatically manages binding and unbinding from the Vault's remote service when making API calls.

When you are completely finished using the SDK, or when your component (e.g., an Activity or ViewModel) is destroyed, you should explicitly disconnect to release internal resources.

```kotlin
override fun onCleared() {
    super.onCleared()
    sakshiClient.disconnect()
}
```
