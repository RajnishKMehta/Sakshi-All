# Sākṣī Ecosystem 📱⚡

**Sākṣī** ("Witness" in Sanskrit) is an open-source Android ecosystem designed to protect important media—photos, videos, and other files—from accidental deletion, deliberate destruction, or unauthorized tampering.

When recording evidence of wrongdoing, intimidation, corruption, or abuse of power, individuals often face pressure to delete their recordings. Sākṣī addresses this by transparently copying captured media into an independent, secure storage layer (the **Vault**) over Android IPC. Even if a bad actor forces the user to delete the recording from the camera app, a preserved copy remains safely hidden in the background.


---

## 🏗️ Ecosystem Components

Sākṣī consists of four modular, independent projects that work together. They communicate strictly over local inter-process communication (IPC) and do not rely on an internet connection.

### 1. [Sākṣī SDK](../01_sdk/README.md)
The core communication bridge. This lightweight, headless Android client provides the Kotlin/coroutines APIs needed for client apps (like Sākṣī Camera) and the Vault to talk to each other securely over AIDL without sharing databases or storage directories.

### 2. [Sākṣī Vault](../02_vault/README.md)
The independent, background-only secure storage application. It receives media incrementally, manages persistence, and schedules delayed deletions. Because it has no user interface, its surface area for compromise is minimal.

### 3. [Sākṣī Camera](../03_camera/README.md)
The capture application. It records photos and videos (including crash-safe fragmented MP4 formats) and uses the Sākṣī SDK to immediately mirror that media to the Vault while the recording is still ongoing.

### 4. [Sākṣī Portal](../04_portal/README.md)
The user-facing management application. Built entirely with Jetpack Compose, it securely connects to the Vault to allow users to browse, view, and safely export their preserved media when the threat has passed.

---

## 📖 Documentation & Guides

Technical documentation and GitBook links will be added here once the documentation website is finalized.

---
## 🤝 Contributing

Contributions are welcome. Please read the `CONTRIBUTING.md` and `CODE_OF_CONDUCT.md` files located in each respective repository before contributing.
