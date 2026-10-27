# Sākṣī SDK

The Sākṣī SDK (`rajnishkmehta.sakshi.sdk`) is a lightweight, headless Android client and IPC library designed for local-first inter-process communication within the Sākṣī ecosystem.

It serves as the bridge between trusted client applications (such as Sākṣī Camera or Sākṣī Portal) and the independent Sākṣī Vault application.

## About

The Sākṣī SDK provides clean, idiomatic, coroutine-powered Kotlin public APIs. It abstracts away complex Binder proxy transactions and AIDL interfaces, allowing client applications to easily send photos, manage incremental audio/video synchronization, query recording status, and browse stored media safely in the Vault.

## Features

- **Headless IPC Bridge**: Facilitates secure inter-process communication without sharing databases or directories.
- **Client & Vault APIs**: Provides `SakshiClient` for client applications and `VaultResponder` for Vault service implementations.
- **Incremental Sync**: Supports incremental file copying and A/V sync, essential for crash-safe recording formats.
- **Flow-based Status**: Emits sync states and file copy acknowledgements (`CopyDoneAck`) using Kotlin Flows.
- **Strictly No UI**: Contains absolutely no user interface components (Activities, Fragments, Compose, or XML layouts).

## Architecture / How It Works

The SDK orchestrates communication across two distinct domains:

1. **Client Applications (Camera/Portal)**: Use `SakshiClient` to initiate media transfers, sync video, ping the Vault, or query stored media.
2. **Vault Application**: Implements the AIDL remote service and uses `VaultResponder` to send structured responses, sync progress, copy completions, and error events back to the connected client.

## Requirements / Compatibility

- Minimum Android Version: Android 10 (API Level 29)
- Language: Kotlin

## Installation / Usage

Add the Sākṣī SDK to your app's `build.gradle.kts` dependencies. The SDK is published to Maven Central and GitHub Packages.

```kotlin
dependencies {
    implementation("io.github.rajnishkmehta.sakshi:sakshi-sdk:1.0.0-RC.3")
}
```

## Documentation

Detailed technical specifications and integration guides are available in the SDK repository's `docs` directory:

- [System Architecture](docs/architecture.adoc)
- [Client API Usage](docs/api_usage.adoc)
- [Vault Integration Guide](docs/vault_integration.adoc)
- [AIDL IPC Specification](docs/ipc_specification.adoc)
- [Development Guide](docs/development.adoc)

## License

This project is licensed under the Apache License 2.0. See the LICENSE file for details.
