---
description: "The Sākṣī ecosystem separates capture, communication, storage, and media management into independent components. This modular design lets each..."
icon: book-open
---
# Ecosystem Overview

The Sākṣī ecosystem separates capture, communication, storage, and media management into independent components. This modular design lets each application focus on one responsibility while communicating locally with the Vault.

## Component Responsibilities

1. **[Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera)** handles camera and microphone interactions, captures photos and videos, and sends supported media to Vault through the SDK.
2. **[Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal)** provides the interface for browsing, viewing, managing, and exporting media stored in Vault.
3. **[Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK)** provides the client-side APIs and Android IPC integration used by Camera and Portal to communicate with Vault.
4. **[Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault)** is the background storage service. It receives media, stores it in its app-private storage, and maintains file and synchronization metadata.
5. **Sākṣī Voice Recorder (planned)** is a future audio-recording application intended to use the same Vault workflow. It is not currently available.

## Media Flow Architecture

The following diagram shows the local data flow between the Sākṣī components. Media transfer and service communication are designed to work without an internet connection.

```mermaid
---
config:
  theme: base
  themeVariables:
    background: "#F8FAFC"
    primaryColor: "#EEF2FF"
    primaryTextColor: "#312E81"
    primaryBorderColor: "#6366F1"
    secondaryColor: "#ECFDF5"
    secondaryTextColor: "#065F46"
    tertiaryColor: "#F0FDFA"
    lineColor: "#64748B"
    textColor: "#1E293B"
    edgeLabelBackground: "#FFFFFF"
    fontSize: "15px"
    fontFamily: "Inter, Arial, sans-serif"
  flowchart:
    nodeSpacing: 56
    rankSpacing: 72
    diagramPadding: 20
    curve: linear
    htmlLabels: false
---
flowchart TB

    C["Sākṣī Camera"]
    P["Sākṣī Portal"]
    SDK["Sākṣī SDK"]
    V["Sākṣī Vault Service"]
    DB[("Room3 Database")]
    FS[("Private File Storage")]

    C <-->|"SDK API"| SDK
    P <-->|"SDK API"| SDK
    SDK <-->|"Local IPC / AIDL"| V
    V --> DB
    V --> FS

    classDef client fill:#EEF2FF,stroke:#6366F1,color:#312E81,stroke-width:2px
    classDef sdk fill:#4338CA,stroke:#312E81,color:#FFFFFF,stroke-width:3px
    classDef vault fill:#CCFBF1,stroke:#0D9488,color:#134E4A,stroke-width:2px
    classDef database fill:#DBEAFE,stroke:#3B82F6,color:#1E3A8A,stroke-width:2px
    classDef filesystem fill:#DCFCE7,stroke:#16A34A,color:#14532D,stroke-width:2px

    class C,P client
    class SDK sdk
    class V vault
    class DB database
    class FS filesystem
```

For a deeper technical breakdown of the IPC and component interaction, see the [Detailed Architecture Flowchart](../developer-guide/concepts/architecture-flow.md).

## Important Security Boundary

Vault stores media in its own application storage, but this alone should not be interpreted as protection against every application-level or device-level attack. Actual access control depends on the exported service and provider configuration, Android permissions, and the device's security state. Do not treat Sākṣī as a substitute for secure device configuration or an independent off-device backup.

## Continue Reading

- **[User Guide](../user-guide/index.md):** learn how to install and use the Sākṣī applications.
- **[Developer Guide](../developer-guide/README.md):** understand the architecture and integrate with the Sākṣī ecosystem.
