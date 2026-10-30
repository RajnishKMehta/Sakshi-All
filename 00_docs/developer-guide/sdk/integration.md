# SDK Integration

Integrating the Sākṣī SDK allows your Android application to securely transmit media to or query media from the Vault application.

## Minimum Requirements

*   **Android API Level**: The Sākṣī SDK and all its components require a minimum SDK version of API 29 (Android 10).
*   **Kotlin Coroutines**: The SDK relies entirely on Kotlin coroutines for asynchronous operations.

## Dependency Configuration

To use the SDK, you must include it in your client application's Gradle build. Because the Sākṣī ecosystem utilizes Gradle composite builds, ensure your client application specifies the dependency correctly.

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    implementation(project(":sakshi-sdk"))
}
```

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

The entry point to the SDK is the `SakshiClient` interface. You instantiate it using `SakshiClient.create()`, passing in an application or component `Context` and an optional `SakshiClientConfig`.

### Default Configuration

By default, the SDK is configured to connect to the standard Vault package and intent action.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

// Using the application context is recommended to prevent memory leaks
val sakshiClient = SakshiClient.create(applicationContext)
```

### Custom Configuration

If you need to connect to a different package name or modify the connection timeout, you can provide a custom `SakshiClientConfig`.

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

The `SakshiClient` automatically manages binding and unbinding from the Vault's remote service when making API calls.

When you are completely finished using the SDK, or when your component (e.g., an Activity or ViewModel) is destroyed, you should explicitly disconnect to release internal resources.

```kotlin
override fun onCleared() {
    super.onCleared()
    sakshiClient.disconnect()
}
```
