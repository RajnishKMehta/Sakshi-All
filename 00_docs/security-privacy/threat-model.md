# Security Limitations & Threat Model

Sākṣī is designed with a specific threat model in mind: preventing the loss of important media when a user is forced or pressured to delete the visible, original copy from their standard gallery.

It is crucial to understand what Sākṣī is designed to protect against and, equally importantly, what it is **not** designed to protect against.

## In Scope: What Sākṣī Protects Against

Sākṣī's architecture mitigates the following risks:

*   **Forced Deletion of Originals:** If an authority figure or malicious individual forces you to delete a video from your normal camera roll or gallery, the Sākṣī Vault retains its separate, synchronized copy.
*   **Unexpected Device Shutdown (fMP4 only):** When using the default fragmented MP4 (fMP4) recording mode in Sākṣī Camera, video data is incrementally synchronized to Vault during recording. If the device crashes, loses power, or the app is killed mid-recording, the synchronized fragments remain safe in Vault.
*   **Casual Device Browsing:** If someone briefly inspects your phone, they will not see Vault's media in the standard gallery. Sākṣī Portal can be protected by App Lock (device credentials/biometrics), and if the Portal app is uninstalled, the interface to access the Vault is removed while the data remains intact in Vault's private storage.

## Out of Scope: What Sākṣī Does Not Protect Against

Sākṣī is **not** a comprehensive security suite, an encrypted vault, or an invisibility cloak. It does not protect against the following threats:

### 1. Advanced Physical Extraction or Forensics
Sākṣī stores media as standard, unencrypted files within Vault's application-private storage. If an adversary gains privileged (root) access to the device, or performs a physical forensic extraction of the device's storage, the Sākṣī Vault data can be read.

### 2. Lack of Encryption at Rest
Vault relies entirely on the standard Android OS sandboxing to protect its private storage directory. Sākṣī itself **does not encrypt** the media files, database, or thumbnails at rest. Do not rely on Sākṣī if your threat model requires strong cryptographic protection of data at rest.

### 3. Device Confiscation and Factory Reset
If the device is taken from you permanently, or if the device is factory reset, the Sākṣī Vault and all data within it will be destroyed. Sākṣī operates entirely locally and does not automatically back up data to the cloud.

### 4. Forensic Data Recovery (No Secure Erase)
When media is permanently deleted from the Vault, the file is deleted using standard operating system deletion commands (`file.delete()`). Sākṣī **does not perform secure erasure** (e.g., overwriting the file with zeros). Due to the nature of modern flash storage (wear leveling, TRIM), securely erasing individual files is unreliable on Android. Deleted files may be recoverable by specialized forensic tools until the flash storage blocks are physically overwritten by the OS.

### 5. Exported Copies
When you export media from the Vault to external storage (e.g., the Downloads folder or MediaStore) using Sākṣī Portal, that exported copy is completely outside the protection of the Sākṣī ecosystem. It will be visible to other apps and the standard gallery.

### 6. App Uninstallation
If the **Sākṣī Vault** application itself is uninstalled, Android will automatically delete its private storage directory, permanently destroying all preserved media. Uninstalling Sākṣī Portal or Sākṣī Camera does not affect Vault's data, but uninstalling Vault destroys the data. Sākṣī cannot prevent a user from uninstalling the Vault app through device settings.

### 7. Guaranteed Invisibility
The Sākṣī Vault is a separate background application, which reduces casual visibility. However, its presence can easily be discovered by checking the list of installed applications in Android System Settings, viewing active services, or using device management tools.
