#!/bin/bash
sed -i '/override fun onCreate(savedInstanceState: Bundle?)/a \
        rajnishkmehta.sakshi.camera.logging.Logger.i("MainActivity", "onCreate called")' 03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/MainActivity.kt

sed -i '/private fun checkPermissions() {/a \
        if (!hasCameraPermission()) rajnishkmehta.sakshi.camera.logging.Logger.e("MainActivity", "Permissions missing")' 03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/MainActivity.kt

sed -i '/private fun showAudioPermissionDeniedDialog(onDisableAudio: () -> Unit = {}) {/a \
        rajnishkmehta.sakshi.camera.logging.Logger.w("MainActivity", "Audio permission denied")' 03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/MainActivity.kt
