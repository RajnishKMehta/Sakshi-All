<div align="center">
  <h1>Sākṣī (साक्षी)</h1>
  <p><b>Preserving a separate, independent copy of important media.</b></p>
  <p><i>Sākṣī ≈ Sakshi</i></p>
</div>

---

Sākṣī (meaning "Witness" in Sanskrit) is an open-source Android ecosystem designed to help you retain a separate copy of personal, private, or important media. When capturing evidence of wrongdoing, people may be pressured to delete recordings from their phones. Sākṣī helps reduce the risk of losing the only copy by storing an independent copy in Vault. Once Vault has successfully received the media, deleting the ordinary original from the phone's gallery does not delete the copy stored in Vault.

## The Ecosystem

Sākṣī is made up of four independent components that work together. You can use the ready-made Android applications or integrate the SDK into your own application.

### Sākṣī Vault
**The storage service**

A background-only application with no user interface. Vault receives and stores independent copies of media, maintains synchronization metadata, and supports scheduled deletion workflows. It is designed to work locally without requiring an internet connection.

[Explore Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault)

### Sākṣī Camera
**The capture application**

Capture photos and videos and synchronize them with Vault. Fragmented MP4 (fMP4) recordings can be synchronized incrementally while recording, helping preserve already-written fragments if recording is interrupted.

[Explore Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera)

### Sākṣī Portal
**The media interface**

Browse, play, manage, and export media stored in Vault. Portal works locally and does not upload your private media to a cloud service.

[Explore Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal)

### Sākṣī SDK
**The developer library**

A lightweight, headless Android library that provides the IPC APIs client applications use to communicate with the Vault service and transfer media.

[Explore Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK)

> **Read the full [Ecosystem Overview](ecosystem-overview.md) to understand how these components interact.**

## Documentation Structure

This repository is organized into specific guides based on your needs:

### For Users

If you want to install and use the Sākṣī applications on your device, start with the **[User Guide](../user-guide/index.md)**.
*   [Installation Instructions](../user-guide/getting-started/installation.md)
*   [Using Sākṣī Camera](../user-guide/camera/recording-and-sync.md)
*   [Managing Media with Sākṣī Portal](../user-guide/portal/browsing-and-exporting.md)

### For Developers

If you want to understand the system architecture, contribute to the projects, or integrate your own application with Sākṣī Vault, refer to the **[Developer Guide](../developer-guide/README.md)**.
*   [Architecture Flow](../developer-guide/concepts/architecture-flow.md)
*   [SDK Integration](../developer-guide/sdk/integration.md)
*   [API Reference](../developer-guide/sdk/api/README.md)

### Security and Privacy

Understanding the security boundaries and privacy guarantees of Sākṣī is critical. Sākṣī is an offline-first system, but it has specific limitations.

*   Read the **[Security & Privacy Overview](../security-privacy/README.md)** to understand the threat model and what Sākṣī can and cannot protect against.
*   Review our comprehensive **[Privacy Policy](../security-privacy/privacy-policy.md)** to understand how your data remains on your device.

## Core Philosophy

*   **Local First:** Sākṣī applications do not require an internet connection to function.
*   **Separation of Concerns:** Capture, storage, and management are intentionally kept separate to maximize resilience.
*   **User Control:** Your data is stored in the Vault, and you control when (or if) it is exported back to the public filesystem.
