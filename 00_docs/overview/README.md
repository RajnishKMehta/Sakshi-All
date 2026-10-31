# Sākṣī Ecosystem Overview

Welcome to the documentation for the Sākṣī ecosystem.

Sākṣī (साक्षी) is a suite of independent, local-first Android applications designed to solve a specific problem: preserving a separate copy of important media so that forced or accidental deletion of the visible original does not destroy the evidence.

This documentation serves as the central guide for understanding, using, and integrating with Sākṣī.

## What is Sākṣī?

At its core, Sākṣī provides a mechanism to safely capture and store media (photos and videos) separately from the standard Android Gallery or file manager.

It achieves this through a modular design involving four main components:

1.  **[Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault):** The secure, background storage service.
2.  **[Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera):** A capture application with specialized, crash-safe recording capabilities.
3.  **[Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal):** The user interface for managing and viewing preserved media.
4.  **[Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK):** The integration library that allows these independent apps to communicate.

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
