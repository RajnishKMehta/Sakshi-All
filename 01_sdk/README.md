# Sākṣī SDK

**Kotlin APIs for communication between Sākṣī client apps and Vault.**

Sākṣī SDK is a headless Android library that provides a Kotlin and coroutine-based API for local inter-process communication (IPC) with Sākṣī Vault. It helps client apps request media operations without directly accessing Vault's private storage or database.

## Features

- Client-side communication through Android Binder/AIDL.
- `SakshiClient` APIs for supported media operations and Vault interaction.
- `VaultResponder` helpers for implementing the Vault-side service.
- Structured responses, synchronization progress, acknowledgements, and error handling, according to the exposed API.
- No UI components.

## Installation

Add Maven Central and the dependency to your app or library module's `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.rajnishkmehta.sakshi:sakshi-sdk:1.0.0-RC.3")
}
```

The version above matches the version declared in this repository's version catalog. Check the [Maven Central artifact](https://central.sonatype.com/artifact/io.github.rajnishkmehta.sakshi/sakshi-sdk) for the latest published release before upgrading.

## Compatibility

- Android 10 (API 29) and newer, as configured by the project.
- Kotlin and Android Gradle tooling versions must be compatible with this repository's build configuration.

## Documentation

See the SDK's source and API documentation in this repository. For the connected applications, visit [Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera), [Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault), and [Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal).
