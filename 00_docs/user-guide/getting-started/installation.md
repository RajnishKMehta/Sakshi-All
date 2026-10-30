# Install Sākṣī

Sākṣī is a modular Android ecosystem. Install the components you need; you do not have to install every component.

The **Vault** stores the independent copy of your media. **Camera** captures media, while **Portal** lets you browse and manage what Vault has stored. Camera can also be used as a regular camera without Vault, but the Sākṣī preservation workflow requires Vault to be installed.

## Requirements

- **Android:** Android 10 (API level 29) or newer.
- **Storage:** enough free local storage for the media you capture.

## Download the Applications

**Recommended installation order:** install Vault first, then install Camera and/or Portal.

### [Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault)

[![Latest Sākṣī Vault release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Vault?label=Latest%20Vault&style=for-the-badge&color=blue&logo=android)](https://github.com/RajnishKMehta/Sakshi-Vault/releases/latest)

Sākṣī Vault is the core storage component. **It must be installed before you can use the other apps.**

1. Download the latest Vault APK from the [Sākṣī Vault Releases](https://github.com/RajnishKMehta/Sakshi-Vault/releases/latest).
2. Install the application on your Android device.

**Important Note about Vault:**
Sākṣī Vault runs entirely in the background. It does **not** have a user interface, and it will **not** appear in your app drawer. You cannot open it. There is no manual setup or configuration required. Simply installing it is all that is needed.

### [Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera)

[![Latest Sākṣī Camera release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Camera?label=Latest%20Camera&style=for-the-badge&color=green&logo=android)](https://github.com/RajnishKMehta/Sakshi-Camera/releases/latest)

Use this app to capture photos and crash-safe videos that are automatically sent to the Vault.

1. Download and install the [Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera/releases/latest).
2. Open the Camera app.
3. Grant the required permissions (Camera, Microphone) when prompted.
4. The Camera will automatically detect and connect to the installed Vault.

### [Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal)

[![Latest Sākṣī Portal release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Portal?label=Latest%20Portal&style=for-the-badge&color=purple&logo=android)](https://github.com/RajnishKMehta/Sakshi-Portal/releases/latest)

Use this app to view, manage, and export the media stored in your Vault.

1. Download and install the [Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal/releases/latest).
2. Open the Portal app.
3. The Portal will automatically detect and connect to the installed Vault.

## How the Applications Work Together

1. Install Vault to provide local storage for preserved media.
2. Install Camera to capture photos and videos. With Vault installed, supported media is transferred to it automatically.
3. Open Portal whenever you want to browse, play, manage, or export media stored in Vault.

## No Manual Pairing Required

For the official Sākṣī applications, you do not need to pair or manually configure Camera and Portal with Vault. They detect the installed official Vault and connect to it automatically.

## Continue Reading

- [Camera: Recording and Sync](../camera/recording-and-sync.md)
- [Portal: Browsing and Exporting](../portal/browsing-and-exporting.md)
