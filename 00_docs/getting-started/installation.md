# Installation

Getting started with Sākṣī involves installing the independent applications that make up the ecosystem. Because Sākṣī is composed of modular apps, you only need to install the components you intend to use.

## Requirements

* **Android Version**: All Sākṣī applications require a minimum of **Android 10 (API Level 29)** or newer.
* **Storage**: Sufficient local device storage to hold your captured media. Sākṣī does not use cloud storage; all data is kept locally on your device.

## Core Applications

To fully utilize the ecosystem for capturing, preserving, and viewing media, you will need to install three separate applications:

1. **Sākṣī Vault**: The mandatory background storage service.
2. **Sākṣī Camera**: The application used to capture photos and videos.
3. **Sākṣī Portal**: The application used to view and manage your saved media.

*(Note: The Sākṣī SDK is a developer library included within the Camera and Portal apps, and does not need to be installed separately by users).*

## Installation Steps

Currently, the Sākṣī applications are available via their respective GitHub repositories. Public download links or App Store releases may not be universally available yet, so you will need to download the APK files directly from the GitHub Releases pages.

### Step 1: Install Sākṣī Vault

You must install Sākṣī Vault first, as the Camera and Portal depend on it to function securely.

1. Navigate to the [Sākṣī Vault Releases](https://github.com/RajnishKMehta/Sakshi-Vault/releases) page.
2. Download the latest `.apk` file under the "Assets" section.
3. Open the downloaded file on your Android device and proceed with the installation.
4. *Important:* You will not see an app icon for Vault in your app drawer because it is designed as a background-only service without a user interface.

### Step 2: Install Sākṣī Camera

Once the Vault is installed, you can install the capture application.

1. Navigate to the [Sākṣī Camera Releases](https://github.com/RajnishKMehta/Sakshi-Camera/releases) page.
2. Download the latest `.apk` file.
3. Install the application on your device.
4. Upon opening Sākṣī Camera for the first time, grant the necessary permissions (Camera and Microphone) so it can function.

### Step 3: Install Sākṣī Portal

To view the media you have captured and safely stored in the Vault, install the Portal interface.

1. Navigate to the [Sākṣī Portal Releases](https://github.com/RajnishKMehta/Sakshi-Portal/releases) page.
2. Download the latest `.apk` file.
3. Install the application on your device.

## Working Together

Once installed, the applications immediately begin working together securely on your device:

* When you open the **Sākṣī Camera** and record a video or take a photo, it automatically uses local Inter-Process Communication (IPC) to securely transmit the file to the **Sākṣī Vault**.
* Because the Vault is a background service, this storage happens silently.
* When you want to view the stored media, you open the **Sākṣī Portal**. The Portal securely requests access from the Vault to display your photos and playback your videos, entirely offline.
