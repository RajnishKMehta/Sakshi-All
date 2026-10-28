# Sākṣī SDK

Sākṣī SDK is the official Android client and IPC library for the Sākṣī ecosystem. It provides the communication bridge between client applications (such as a camera) and the independent Sākṣī Vault.

By using this SDK, client apps can send photos, synchronize videos incrementally, and schedule media deletion without needing to manage the Vault's underlying storage or database mechanisms.

## About

Sākṣī helps people retain important media and evidence by keeping a separate copy beyond the ordinary gallery or file manager. The SDK is the headless communication layer that makes this possible. It handles Android Binder/AIDL complexity, providing clean, coroutine-powered Kotlin APIs for developers building tools that integrate with the Vault.

## Features

- **Local IPC Communication:** Securely connect to the Vault application using Android Binder (AIDL).
- **Media Transfer:** Send photos and initiate incremental audio/video synchronization.
- **Background Sync Management:** Pause, resume, and stop video synchronization.
- **Media Management:** Query recording status, list preserved media, and schedule media for delayed deletion.
- **Headless Design:** Contains no UI components; strictly a communication library.
- **Coroutine Support:** Clean, non-blocking execution using Kotlin Coroutines and structured concurrency.

## Installation

Add the dependency to your app's `build.gradle.kts` file:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.rajnishkmehta.sakshi:sakshi-sdk:1.0.0-RC.3")
}
```

The SDK requires a minimum Android version of 10 (API 29).

## Architecture

The Sākṣī ecosystem relies on separated responsibilities:

- **Client App (e.g., Camera or Portal):** Captures or views media and uses the `SakshiClient` to send commands and data.
- **Sākṣī SDK (This Project):** Marshals these commands across the IPC bridge to the Vault service.
- **Sākṣī Vault:** The background service that receives data, performs incremental copying, manages persistence, and handles delayed deletions.

## Documentation

For detailed technical specifications and integration guides, see the `/docs` directory:
- [Client API Usage](docs/api_usage.adoc)
- [Vault Integration Guide](../02_vault/docs/INTEGRATION.md)
- [System Architecture](docs/architecture.adoc)
- [AIDL IPC Specification](docs/ipc_specification.adoc)
- [Development Guide](docs/development.adoc)

## License

This project is licensed under the Apache License 2.0.
