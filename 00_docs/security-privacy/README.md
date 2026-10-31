# Security Overview

The Sākṣī ecosystem is designed to address a specific problem: preserving a separate copy of important media so that forced or accidental deletion of the visible original does not destroy the evidence. It does this by separating the capture, storage, and management of media into independent local applications.

## Security Architecture

The Sākṣī architecture is built upon a local-first, offline-capable design. There are four primary components, each with distinct responsibilities and security boundaries:

1. **[Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault):** The background storage service. It acts as the definitive source of truth for preserved media. It stores files in its application-private internal storage, managed by a Room3 database. Vault operates entirely locally and requires no internet connection.
2. **[Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera):** The capture application. It records media and communicates with Vault via the SDK to synchronize copies. Camera has no direct access to Vault's internal files.
3. **[Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal):** The user interface for managing Vault media. It communicates with Vault via the SDK to retrieve, display, and export media. Portal can optionally require device authentication (App Lock) before allowing access to its interface.
4. **[Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK):** The inter-process communication (IPC) bridge. It handles the local AIDL communication between the client apps (Camera, Portal) and the Vault.

## Trust Boundaries

The core security principle of Sākṣī is that the Vault's copy of media is separate from the standard Android gallery or file manager.

*   **Client to Vault:** Client applications (like Camera and Portal) never directly read or write to Vault's storage directory. All interactions must go through the SDK's defined IPC interface.
*   **Vault Storage:** Vault stores media in its private application storage (`context.filesDir`). On a standard, unrooted Android device, this storage is isolated and inaccessible to other applications or casual file browsers.
*   **Local Communication Only:** The SDK and Vault use Android's Binder mechanism for local IPC. No data is transmitted over a network or the internet during this communication.

## What Sākṣī Provides

*   **Separation of Data:** Deleting a photo or video from the standard Android gallery does not delete the copy synchronized to the Sākṣī Vault.
*   **Crash-Safe Recording (fMP4):** Sākṣī Camera uses fragmented MP4 (fMP4) by default. This allows the video to be synchronized incrementally to Vault while the recording is still in progress. If the device shuts down unexpectedly, the synchronized fragments are preserved in Vault. (Standard MP4 files are only copied after the recording finishes).
*   **Reduced Visibility:** Because Vault is a background service and Portal can be uninstalled without deleting Vault's data, the preserved media is not immediately visible in a casual inspection of the device.

## Important Limitations

Sākṣī provides an additional layer of media preservation, **not an absolute guarantee of security or invisibility**.

*   Sākṣī does **not** encrypt media at rest.
*   Sākṣī does **not** securely erase (wipe) deleted files from flash storage.
*   Sākṣī does **not** prevent a sufficiently motivated adversary with physical access and advanced tools (or device root access) from recovering data.

For a detailed explanation of what Sākṣī is and is not designed to protect against, read the [Threat Model & Security Limitations](threat-model.md).
