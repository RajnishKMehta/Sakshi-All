# Sākṣī

Sākṣī ("Witness" in Sanskrit) is an open-source Android ecosystem designed to help people retain important media and preserve evidence.

When individuals record evidence of corruption, misconduct, or abuse of authority, they often face pressure to delete their recordings. Sākṣī addresses this specific problem by preserving a separate copy of important media. This ensures that deleting the visible original from the ordinary gallery does not necessarily destroy the preserved copy.

Sākṣī is intended to provide an additional layer of media preservation, not an absolute guarantee of security. It does not provide immunity from device seizure, privileged system access, or physical destruction.

## Ecosystem Components

Sākṣī consists of four independent Android projects that work together:

- **[Sākṣī Camera](../03_camera/README.md):** Records photos and videos. It can be used independently or integrated with the Sākṣī ecosystem to synchronize media.
- **[Sākṣī Vault](../02_vault/README.md):** A background-only application that securely stores and manages a separate copy of media received from compatible applications.
- **[Sākṣī Portal](../04_portal/README.md):** The user-facing application for browsing, viewing, exporting, and managing media stored in the Vault.
- **[Sākṣī SDK](../01_sdk/README.md):** The Android local inter-process communication (IPC) client library that connects the Camera, Portal, and other applications to the Vault.

## Documentation

Detailed documentation will be added here once it is published. For now, please refer to the README files in the individual component repositories linked above.

## Contributing

Contributions are welcome. Please read the `CONTRIBUTING.md` and `CODE_OF_CONDUCT.md` files located in each respective repository before contributing.
