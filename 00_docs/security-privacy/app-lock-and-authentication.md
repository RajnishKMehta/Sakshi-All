# App Lock & Authentication

Sākṣī Portal provides a user interface to browse and manage the media stored in Sākṣī Vault. To prevent unauthorized access to this interface by someone who casually picks up an unlocked device, Portal includes an **App Lock** feature.

## What App Lock Protects

App Lock is a UI-level gatekeeper within the Sākṣī Portal application. When enabled, the user must authenticate before they can:
*   View the list of media in the Vault.
*   Play videos or view photos.
*   Export, delete, or recover media.
*   Change Portal settings.

## Authentication Methods

Sākṣī Portal relies on the Android platform's `BiometricPrompt` API for authentication. It supports:

*   **Device Credentials:** The PIN, pattern, or password used to unlock the phone.
*   **Biometrics (Class 2 / Weak or higher):** Fingerprint or face unlock, depending on the device's hardware capabilities.

Sākṣī **does not** implement its own custom PIN or password system. It leverages the secure authentication methods already established by the user in Android OS settings.

## Limitations of App Lock

It is critical to understand the boundaries of what App Lock provides:

1.  **UI Protection Only:** App Lock only prevents access to the *Portal interface*. It does not encrypt the underlying files stored in Sākṣī Vault.
2.  **Does Not Block IPC:** App Lock does not prevent other applications (like Sākṣī Camera) from using the Sākṣī SDK to send media to the Vault.
3.  **Authentication State:** App Lock relies on the OS biometric hardware. If the OS biometric system is compromised, or if a malicious actor knows the device's lock screen PIN, they can bypass the App Lock.

App Lock is designed to prevent casual snooping on an already unlocked device, not to defend against an attacker who has physical control of the device and knows the device PIN.
