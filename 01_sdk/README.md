# Sākṣī SDK

Sākṣī SDK is the Android local inter-process communication (IPC) client library for the Sākṣī ecosystem.

It provides the Kotlin/coroutines APIs needed for client applications (such as Sākṣī Camera and Sākṣī Portal) and Sākṣī Vault to communicate securely over Android AIDL without sharing databases or storage directories.

## Features

- **Inter-Process Communication:** Connects client applications securely with the Vault service.
- **Headless Design:** Strictly a communication bridge without any UI components, XML layouts, or visual elements.
- **Coroutines Support:** Provides clean, idiomatic Kotlin coroutine-powered public APIs for easy integration.
- **Media Transfer APIs:** APIs for sending files, querying status, and managing media (like requesting delayed deletion or recovery).
- **Incremental Synchronization APIs:** Specific APIs for managing incremental synchronization passes.

## Architecture

The SDK enables dual-side architecture:

1. **Client Applications (e.g., Camera, Portal):** Use the SDK (`SakshiClient`) to send files, start/pause/stop incremental synchronization, query the Vault, and manage media.
2. **Vault Application:** Uses the SDK (`VaultResponder`) to implement the remote AIDL service binding, handle background copying, and send status updates and completion acknowledgments back to clients.

## Installation

Add the dependency to your application's `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.rajnishkmehta.sakshi:sakshi-sdk:1.0.0-RC.3")
}
```

## Documentation

Detailed technical specifications and integration guides are available in the [`/docs`](docs) directory:

- [System Architecture](docs/architecture.adoc)
- [Client API Usage](docs/api_usage.adoc)
- [Vault Integration Guide](docs/vault_integration.adoc)
- [AIDL IPC Specification](docs/ipc_specification.adoc)
- [Development Guide](docs/development.adoc)

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
