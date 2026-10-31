# Sākṣī Ecosystem Privacy Policy

**Effective Date:** 2026-10-31

This Privacy Policy applies to the entire Sākṣī ecosystem, which includes **Sākṣī Camera**, **Sākṣī Vault**, **Sākṣī Portal**, the **Sākṣī SDK**, and any associated offline services provided by the Sākṣī project.

The core privacy principle of Sākṣī is that **your data belongs to you and remains on your device.** The Sākṣī ecosystem is designed to operate entirely locally, offline, and without collecting personal information.

## 1. About Sākṣī

Sākṣī is a suite of independent, local-first Android applications designed to help users preserve important media by maintaining a separate, synchronized copy of photos and videos in a private vault. It is not a cloud service.

## 2. Information and Data Handled

Sākṣī applications handle the media you explicitly choose to record or manage using them. This includes:

*   **Media Files:** Photos and videos captured by Sākṣī Camera, or transferred into Sākṣī Vault via the SDK.
*   **Media Metadata:** Information generated during capture or transfer, such as timestamps, file sizes, and file IDs used internally by the ecosystem to manage the files.

## 3. How Data Is Stored (Local Storage Only)

**Sākṣī does not store your data on remote servers or in the cloud.**

All media, thumbnails, and database records are stored entirely on your local device. Specifically, Sākṣī Vault stores media in its application-private internal storage. This storage is generally inaccessible to other applications on an unrooted device.

*Note: Sākṣī does not encrypt your files at rest. The privacy of your stored media depends on the security of your device's operating system.*

## 4. How Applications Communicate (Local IPC Only)

Sākṣī Camera and Sākṣī Portal communicate with Sākṣī Vault using the Sākṣī SDK. This communication occurs strictly via local Inter-Process Communication (IPC) using Android's Binder mechanism. **No data is transmitted over a network or the internet during this process.**

## 5. Data Collection, Analytics, and Crash Reporting

**Sākṣī applications do not collect analytics, telemetry, or crash reports.**

We do not use third-party analytics SDKs (such as Google Analytics or Firebase) to track your usage, behavior, or application crashes. If an application crashes, no automated report is sent to the developers.

## 6. Network and Internet Usage

The core Sākṣī applications (Vault, Camera, and Portal) **do not require internet access to function** and do not transmit your media over the internet.

*   **Sākṣī Camera:** Uses the `ACCESS_NETWORK_STATE` permission solely as an internal dependency requirement for AndroidX Exif/Media3 libraries, but it does not transmit data over the network.
*   **Sākṣī Portal:** Uses local networking libraries (like Coil) to efficiently load and cache images locally, but does not use them to exfiltrate data.

## 7. Data Sharing and Disclosure

Because Sākṣī operates entirely locally and collects no data, **we do not share, sell, or disclose your information to any third parties.**

## 8. Exported Media and User-Controlled Copies

If you use Sākṣī Portal to export a photo or video, that exported copy is saved to a public directory on your device (such as the Downloads folder). Once exported, that specific file is no longer protected by Sākṣī Vault's private storage and will be subject to standard Android visibility rules and whatever cloud backup services you have configured on your device.

## 9. Data Retention and Deletion

You are in full control of your data retention.
*   When you delete a file in Sākṣī Portal, it is moved to a "Scheduled Deletion" state for a grace period.
*   After the grace period expires, or if you choose to permanently delete it, the file and its metadata are deleted from Sākṣī Vault's local storage.
*   Uninstalling the Sākṣī Vault application will instantly and permanently destroy all media stored within it by the Android operating system.

*Note: Sākṣī uses standard file deletion commands. It does not perform forensic secure erasure.*

## 10. Third-Party Libraries

Sākṣī utilizes standard open-source libraries (such as AndroidX, CameraX, Media3, and Coil) to function. These libraries are used strictly for local processing and do not involve remote data collection in the context of the Sākṣī applications.

## 11. Changes to This Policy

We may update this Privacy Policy occasionally to reflect changes in the application ecosystem or legal requirements. We encourage you to review this policy periodically.

## 12. Contact Information

If you have questions about this Privacy Policy or the security practices of the Sākṣī ecosystem, please contact:

**[PLACEHOLDER: Privacy Contact Email / Organization URL]**
