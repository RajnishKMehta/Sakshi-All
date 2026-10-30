# Install Sākṣī

Sākṣī is a modular Android ecosystem. Install the components you need; you do not have to install every component.

The **Vault** stores the independent copy of your media. **Camera** captures media, while **Portal** lets you browse and manage what Vault has stored. Camera can also be used as a regular camera without Vault, but the Sākṣī preservation workflow requires Vault to be installed.

## Requirements

- **Android:** Android 10 (API level 29) or newer.
- **Storage:** enough free local storage for the media you capture.
- **Internet:** not required for normal local communication and storage. Internet access is only needed when downloading the APKs or checking for releases.

## Download the Applications

The badges below show the latest stable release tag for each application. Pre-releases are not included. Select a badge to open that application's latest GitHub release page; the APK can then be downloaded from the release assets.

**Recommended installation order:** install Vault first, then install Camera and/or Portal.

### Sākṣī Vault

[![Latest Sākṣī Vault release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Vault?label=Latest%20Vault&style=for-the-badge&color=blue&logo=android)](https://github.com/RajnishKMehta/Sakshi-Vault/releases/latest)

Vault is a background service and does not provide a normal app-drawer interface. After installing it, there is no manual pairing or initial setup to complete.

### Sākṣī Camera

[![Latest Sākṣī Camera release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Camera?label=Latest%20Camera&style=for-the-badge&color=green&logo=android)](https://github.com/RajnishKMehta/Sakshi-Camera/releases/latest)

Install Camera and grant the requested camera and microphone permissions. When Vault is installed, Camera automatically detects and connects to the official Vault.

### Sākṣī Portal

[![Latest Sākṣī Portal release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Portal?label=Latest%20Portal&style=for-the-badge&color=purple&logo=android)](https://github.com/RajnishKMehta/Sakshi-Portal/releases/latest)

Install and open Portal. It automatically detects and connects to the installed Vault, allowing you to browse and manage stored media.

## How the Applications Work Together

1. Install Vault to provide local storage for preserved media.
2. Install Camera to capture photos and videos. With Vault installed, supported media is transferred to it automatically.
3. Open Portal whenever you want to browse, play, manage, or export media stored in Vault.

## No Manual Pairing Required

For the official Sākṣī applications, you do not need to pair or manually configure Camera and Portal with Vault. They detect the installed official Vault and connect to it automatically.

## Continue Reading

- [Camera: Recording and Sync](../camera/recording-and-sync.md)
- [Portal: Browsing and Exporting](../portal/browsing-and-exporting.md)
- [Troubleshooting](../troubleshooting/common-issues.md)
