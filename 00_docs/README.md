# Sākṣī Documentation

**Preserving a separate, independent copy of important media.**

Sākṣī (साक्षी, meaning "Witness" in Sanskrit) is an open-source Android ecosystem designed to help you securely retain personal, private, or important media. When capturing evidence of wrongdoing, individuals are often pressured or forced to delete recordings from their phones. Sākṣī addresses this risk by keeping a separate copy of your media. Even if the visible original is permanently deleted from your phone's normal gallery, your evidence remains preserved.

## The Sākṣī Ecosystem

Sākṣī consists of four independent but tightly integrated components. Depending on your goals, you can use the ready-made applications or integrate the underlying SDK into your own projects.

### Sākṣī Vault (The Secure Storage)
The core of the ecosystem. Sākṣī Vault is a background-only application without a user interface, dedicated to receiving and securely storing independent copies of your media. It handles synchronization metadata, delayed permanent deletions, and runs entirely offline.
* **[View Sākṣī Vault Repository](https://github.com/RajnishKMehta/Sakshi-Vault)**

### Sākṣī Camera (The Capture App)
A fully functional camera application designed to capture photos and videos and synchronize them with the Vault. It natively supports crash-safe Fragmented MP4 (fMP4) recording, which syncs to the Vault incrementally while you are still recording, helping preserve data even if the recording is unexpectedly interrupted.
* **[View Sākṣī Camera Repository](https://github.com/RajnishKMehta/Sakshi-Camera)**

### Sākṣī Portal (The User Interface)
The gateway for browsing, playing, and managing the files stored in your Vault. Sākṣī Portal is completely offline-capable, guaranteeing your private data never leaves your device unless you explicitly export it. Because it is independent of the Vault, you can uninstall the Portal to remove the viewing interface from your device without immediately deleting the underlying stored data.
* **[View Sākṣī Portal Repository](https://github.com/RajnishKMehta/Sakshi-Portal)**

### Sākṣī SDK (The Developer Library)
A lightweight, headless IPC (Inter-Process Communication) Android library. It provides the standardized APIs required for client applications to securely transfer files and stream video data to the background Vault service.
* **[View Sākṣī SDK Repository](https://github.com/RajnishKMehta/Sakshi-SDK)**

## Security & Privacy Scope

Sākṣī is intended to provide an **additional layer of media preservation**, not an absolute guarantee of security.

**What Sākṣī Does:**
* Preserves a separate copy of media in a background Vault application.
* Reduces the risk of losing the only copy when the ordinary original is deleted from a gallery or file manager.
* Provides incremental, in-progress synchronization for fMP4 recordings.
* Offers an offline, private storage option for important files.

**What Sākṣī Does NOT Do:**
* **It is not an invisibility guarantee.** The Vault's presence can still be discovered through system settings, app lists, or device management tools.
* **It does not withstand device-level compromise.** Sākṣī does not protect against physical seizure, forced factory resets, root-level attacks, or privileged system access.
* **It does not guarantee 100% recovery.** Actual recovery of interrupted recordings depends on the device state, the successful writing of fragments, and the synchronization progress at the moment of interruption.

## Getting Started

Detailed architecture documentation, usage guides, API references, and setup instructions will be published here in the future.

For now, the best place to start is by reviewing the source code and comprehensive READMEs for the individual components:

* **For Users:** Check out the [Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera) and [Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal) repositories to see how the applications work.
* **For Developers:** Explore the [Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK) for integration details, or the [Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault) repository to understand the core storage service architecture.
