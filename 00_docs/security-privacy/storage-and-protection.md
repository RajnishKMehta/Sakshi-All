# Storage & Data Protection

Sākṣī Vault handles the storage of all preserved media in the ecosystem. It is essential to understand where this data is stored and what platform protections apply.

## App-Private Storage

Sākṣī Vault stores all media files, thumbnails, and its Room3 SQLite database within its **application-private internal storage** (specifically, using `Context.getFilesDir()`).

### Security Properties of App-Private Storage

On standard, unrooted Android devices, application-private storage provides a strong isolation boundary enforced by the operating system:

*   **Isolated Access:** Other installed applications (including Sākṣī Camera and Sākṣī Portal) cannot directly read, write, or list the files in Vault's private storage directory.
*   **Invisible to Media Scanners:** Files stored here are not indexed by the Android MediaStore. They will not appear in the standard Gallery, Google Photos, or general file manager applications.
*   **Permissions:** Vault does not require broad external storage permissions (like `READ_EXTERNAL_STORAGE` or `MANAGE_EXTERNAL_STORAGE`) to manage its internal vault.

## No Encryption at Rest

**Sākṣī does not implement application-layer encryption at rest.**

When media is copied into the Vault, the raw file (e.g., the `.mp4` or `.jpg`) is written directly to the private storage directory. It is not encrypted with a password or cryptographic key managed by Sākṣī.

Therefore, the security of the data relies entirely on the integrity of the Android operating system's sandboxing. If the device's security model is compromised (e.g., the device is rooted, or an attacker exploits a privilege escalation vulnerability to gain root access), the files in Vault's private storage can be read in plain text.

## Exported Media

When a user chooses to export a file from Sākṣī Portal, the Portal app writes a copy of that file to a public directory (such as the device's `Downloads` folder) using the `MediaStore` API.

**Once a file is exported, it leaves the Sākṣī security boundary.** The exported file will be:
*   Visible in standard gallery apps.
*   Accessible to any application with external storage permissions.
*   Subject to standard Android deletion behaviors.

Users must be mindful of the copies they choose to export from the Vault.
