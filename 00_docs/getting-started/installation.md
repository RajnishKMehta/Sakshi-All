# Installation

Getting started with Sākṣī involves installing the components you intend to use. You do not need to install every application to use Sākṣī.

For example, **Sākṣī Camera** can be used completely independently as a standard camera application. However, if you wish to use the full media preservation features of the ecosystem, you will need to install the **Sākṣī Vault** to store the media and the **Sākṣī Portal** to view it. The **Sākṣī SDK** is included automatically inside these applications to allow them to communicate securely.

## Requirements

* **Android Version**: All Sākṣī applications require a minimum of **Android 10 (API Level 29)** or newer.
* **Storage**: Sufficient local device storage to hold your captured media. Sākṣī does not use cloud storage; all data is kept locally on your device.

## Download Applications

You can download the latest releases of the Sākṣī applications directly using the links below. We recommend installing the **Sākṣī Vault** first, as the other apps depend on it for storage.

*(Important: You will not see an app icon for Sākṣī Vault in your app drawer because it operates entirely in the background without a user interface).*

[![Download Sākṣī Vault](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Vault?label=Download%20Sākṣī%20Vault&style=for-the-badge&color=blue&logo=android)](https://github.com/RajnishKMehta/Sakshi-Vault/releases/latest/download/Sakshi-Vault.apk)

[![Download Sākṣī Camera](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Camera?label=Download%20Sākṣī%20Camera&style=for-the-badge&color=green&logo=android)](https://github.com/RajnishKMehta/Sakshi-Camera/releases/latest/download/Sakshi-Camera.apk)

[![Download Sākṣī Portal](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Portal?label=Download%20Sākṣī%20Portal&style=for-the-badge&color=purple&logo=android)](https://github.com/RajnishKMehta/Sakshi-Portal/releases/latest/download/Sakshi-Portal.apk)

## Working Together

Once installed, the applications immediately begin working together securely on your device:

* When you open the **Sākṣī Camera** and record a video or take a photo, it automatically uses local Inter-Process Communication (IPC) to securely transmit the file to the **Sākṣī Vault**.
* Because the Vault is a background service, this storage happens silently.
* When you want to view the stored media, you open the **Sākṣī Portal**. The Portal securely requests access from the Vault to display your photos and playback your videos, entirely offline.
