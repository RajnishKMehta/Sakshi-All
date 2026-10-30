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

Currently, Sākṣī is in active development. If pre-built APKs are not available in the respective GitHub Releases pages, you will need to clone the repositories and build the applications from source using Android Studio.

### Step 1: Install Sākṣī Vault

You must install Sākṣī Vault first, as the Camera and Portal depend on it to function securely.

1. Navigate to the [Sākṣī Vault repository](https://github.com/RajnishKMehta/Sakshi-Vault) and check the Releases page for a `.apk` file.
2. If available, download and install the `.apk`. Otherwise, clone the repository and build the project via Android Studio.
3. *Important:* You will not see an app icon for Vault in your app drawer because it is designed as a background-only service without a user interface.

### Step 2: Install Sākṣī Camera

Once the Vault is installed, you can install the capture application.

1. Navigate to the [Sākṣī Camera repository](https://github.com/RajnishKMehta/Sakshi-Camera) and check the Releases page.
2. Download and install the `.apk` file, or build from source.
3. Upon opening Sākṣī Camera for the first time, grant the necessary permissions (Camera and Microphone) so it can function.

### Step 3: Install Sākṣī Portal

To view the media you have captured and safely stored in the Vault, install the Portal interface.

1. Navigate to the [Sākṣī Portal repository](https://github.com/RajnishKMehta/Sakshi-Portal) and check the Releases page.
2. Download and install the `.apk` file, or build from source.

## Working Together

Once installed, the applications immediately begin working together securely on your device:

* When you open the **Sākṣī Camera** and record a video or take a photo, it automatically uses local Inter-Process Communication (IPC) to securely transmit the file to the **Sākṣī Vault**.
* Because the Vault is a background service, this storage happens silently.
* When you want to view the stored media, you open the **Sākṣī Portal**. The Portal securely requests access from the Vault to display your photos and playback your videos, entirely offline.
