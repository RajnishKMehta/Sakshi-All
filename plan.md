1. **Create the Logging System**:
   - Create a `Logger` class in the `debug` source set (`03_camera/app/src/debug/java/rajnishkmehta/sakshi/camera/logging/Logger.kt`).
   - Implement methods to log different levels (`all`, `error`, `warning`, `info`).
   - Each method will write to a respective text file in a `debug_logs` directory using application context. Include a timestamp.
   - Use `read_file` to confirm the `Logger.kt` was successfully written.
   - Create a dummy `Logger` class in the `release` source set (`03_camera/app/src/release/java/rajnishkmehta/sakshi/camera/logging/Logger.kt`) with the same methods but empty implementations (no-op).
   - Use `read_file` to confirm the release `Logger.kt` was successfully written.

2. **Add Logging to Important Places**:
   - In `03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/MainActivity.kt`:
     - Add `rajnishkmehta.sakshi.camera.logging.Logger.i("MainActivity", "onCreate called")` at line 624 (inside `onCreate`).
     - Add `rajnishkmehta.sakshi.camera.logging.Logger.e("MainActivity", "Permissions missing")` at line 452 (inside `checkPermissions`).
     - Add `rajnishkmehta.sakshi.camera.logging.Logger.w("MainActivity", "Audio permission denied")` at line 328 (inside `showAudioPermissionDeniedDialog`).
   - In `03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/MoreSettings.kt`:
     - Add `rajnishkmehta.sakshi.camera.logging.Logger.i("MoreSettings", "onCreate called")` at line 76 (inside `onCreate`).

3. **Create the Debug Logs UI**:
   - Add a `LogsActivity` in the `debug` source set (`03_camera/app/src/debug/java/rajnishkmehta/sakshi/camera/logging/LogsActivity.kt`).
   - Create an XML layout for this activity in `03_camera/app/src/debug/res/layout/activity_logs.xml`.
   - The layout will have 4 buttons for exporting logs (All, Error, Warning, Info) and a button to delete all logs.
   - Implement the export logic using `Intent(Intent.ACTION_CREATE_DOCUMENT)` with `addCategory(Intent.CATEGORY_OPENABLE)` and `type = "text/plain"`, launching it via `registerForActivityResult(ActivityResultContracts.StartActivityForResult())` to save the selected file to the URI provided by the user.
   - Define `LogsActivity` in `03_camera/app/src/debug/AndroidManifest.xml` within a `<manifest>` and `<application>` tag.
   - Use `read_file` to verify the creation and contents of these new UI files.

4. **Integrate the UI into Settings (Debug only)**:
   - Create a `DebugHelper` class in `debug` and `release` source sets (`03_camera/app/src/debug/java/rajnishkmehta/sakshi/camera/logging/DebugHelper.kt` and `03_camera/app/src/release/java/rajnishkmehta/sakshi/camera/logging/DebugHelper.kt`).
   - Define `fun addLogsOption(activity: androidx.appcompat.app.AppCompatActivity, layout: android.widget.LinearLayout)` in both.
   - In `debug/java/.../DebugHelper.kt`, it programmatically creates a `TextView` or `Button` with text "Logs", adds it to `layout` at index 0, and sets an `OnClickListener` that starts `LogsActivity`.
   - In `release/java/.../DebugHelper.kt`, it does nothing.
   - Call this from `03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/MoreSettings.kt` at line 311 (inside `onCreate` just before the window insets), passing `this` and `binding.rootView`.
   - Use `read_file` to confirm the creation of `DebugHelper.kt` and modifications to `MoreSettings.kt`.

5. **Ensure Release Cleanliness via Proguard**:
   - In `03_camera/app/proguard-rules.pro`, add:
     ```
     -assumenosideeffects class rajnishkmehta.sakshi.camera.logging.Logger {
         public static void i(...);
         public static void e(...);
         public static void w(...);
         public static void all(...);
     }
     -assumenosideeffects class rajnishkmehta.sakshi.camera.logging.DebugHelper {
         public static void addLogsOption(...);
     }
     ```
   - Use `read_file` to ensure `03_camera/app/proguard-rules.pro` was appended successfully.

6. **Verify Build**:
   - Run `./gradlew :03_camera:app:assembleDebug` and `./gradlew :03_camera:app:assembleRelease` to verify the changes do not break the build.

7. **Test Changes**:
   - Run relevant tests (e.g., `./gradlew :03_camera:app:test`) to ensure changes do not introduce regressions.

8. **Pre-commit**:
   - Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.

9. **Submit**:
   - Commit and submit changes.
