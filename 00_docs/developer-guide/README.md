# Sākṣī Developer Guide

Welcome to the Sākṣī Developer Guide. This section of the documentation provides the technical details necessary to understand the architecture, contribute to the ecosystem, or integrate your own applications with Sākṣī Vault.

## Getting Started

Sākṣī is built on modern Android architecture principles, utilizing Kotlin, AndroidX Media3, Jetpack Compose, and Room.

If you are new to the project, we recommend starting here:

1.  **[Architecture Overview](concepts/architecture.md):** Understand the core components (Vault, Camera, Portal, SDK) and their distinct responsibilities.
2.  **[Architecture Flow](concepts/architecture-flow.md):** Review the data flow diagrams illustrating how media moves between the applications via local IPC.

## SDK Integration

The **Sākṣī SDK** (`rajnishkmehta.sakshi.sdk`) is a headless Android library that handles the complex Binder/AIDL communication required to transfer files securely to the Vault.

If you want to build a compatible client application (like a custom voice recorder) that saves data to Sākṣī Vault, refer to the SDK documentation:

*   **[Integration Setup](sdk/integration.md):** How to add the SDK dependency and configure your project.
*   **[Connection Lifecycle](sdk/connection-lifecycle.md):** Managing the background service connection to the Vault.
*   **[Error Handling](sdk/error-handling.md):** Understanding and resolving IPC and storage errors.

### API Reference

Detailed documentation for every public SDK method:

*   **[API Index](sdk/api/README.md):** The complete list of available operations.
*   *Key APIs:* [copyFile](sdk/api/copyFile.md), [startAVSync (Crash-Safe Recording)](sdk/api/startAVSync.md), [deleteMedia](sdk/api/deleteMedia.md).

## Security and Trust

Before integrating with Sākṣī or contributing code, you must understand the system's security boundaries.

*   Sākṣī relies on local IPC and Android's application sandboxing.
*   **It does not use encryption at rest.**

Please carefully read the **[Security & Privacy section](../security-privacy/README.md)**, specifically the **[Threat Model](../security-privacy/threat-model.md)** and the **[Unified Privacy Policy](../security-privacy/privacy-policy.md)**, to ensure your technical decisions align with the ecosystem's guarantees and limitations.
