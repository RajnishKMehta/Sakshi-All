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

## Security and Privacy Scope

Sākṣī provides an additional layer of media preservation. It is not an absolute security guarantee.

**What Sākṣī is designed to do**
- Keep a separate copy of media in the Vault application.
- Reduce the risk of losing the only copy when the ordinary original is deleted from a gallery or file manager.
- Synchronize supported fMP4 recordings incrementally while recording.
- Keep stored media on the device without depending on cloud backup.

**What Sākṣī does not guarantee**
- **Invisibility:** Vault may be discoverable through Android settings, installed-app lists, or device-management tools.
- **Protection from device-level compromise:** Sākṣī cannot prevent data loss caused by physical seizure, factory reset, root-level attacks, or privileged system access.
- **Complete recovery:** Recovery of an interrupted recording depends on which fragments were successfully written and synchronized before the interruption.

## Continue Reading

Choose the documentation that matches what you want to do:

- **[User Guide](../user-guide/index.md):** installation, everyday workflows, camera and portal usage, and troubleshooting.
- **Developer Guide:** coming soon. This section will cover SDK integration, architecture, and development workflows.
