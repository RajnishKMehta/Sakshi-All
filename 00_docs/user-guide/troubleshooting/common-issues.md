# Troubleshooting

## Vault Unavailable or Not Found

**Symptom:**
* Sākṣī Portal displays a "Vault Unavailable" or "Vault application not found" message.
* Sākṣī Camera Settings show "Vault Unavailable" under the Vault package setting.

**Cause:**
The application cannot communicate with the Vault. This usually means the Vault app is not installed, or you have selected a custom Vault package that has been uninstalled.

**Solution:**
1. Ensure the official Sākṣī Vault is installed on your device. You can download it using the "Download Vault" button if provided in the app, or from the [official releases page](https://github.com/RajnishKMehta/Sakshi-Vault/releases/latest).
2. If you are trying to use a custom Vault, go into the app's settings (Camera or Portal) and ensure the correct Vault package is selected.

## App Lock Setup Fails

**Symptom:**
When trying to enable App Lock in Portal Settings, you see a message saying: "You must configure a secure lock screen on your device before enabling App Lock."

**Cause:**
Your Android device does not have a PIN, Pattern, Password, or Biometric lock set up in the system settings.

**Solution:**
1. Open your Android device's main Settings app.
2. Go to Security (or Lock Screen / Biometrics).
3. Set up a secure screen lock.
4. Return to Sākṣī Portal and enable App Lock again.

## Portal Shows "Nothing in Vault"

**Symptom:**
The Portal gallery is empty, but you know you took photos or videos using the Camera.

**Cause:**
* The media has not finished synchronizing to the Vault.
* The files were scheduled for deletion.
* The Camera is configured to send files to a different Vault package than the Portal is reading from.

**Solution:**
1. Swipe down on the Portal gallery to refresh it.
2. Check the "Scheduled Deletions" (Recover Media) section in Portal to see if the files are hidden there.
3. Verify that both Camera and Portal are set to use the exact same Vault package in their respective settings.
