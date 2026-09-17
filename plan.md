1. Fix `R.id.debug_logs_setting` resolution issue in release build by creating `src/main/res/values/ids.xml` to declare the ID across all variants.
2. Fix `FileProvider` conflict by changing the authority to `${applicationId}.debug.provider` in `src/debug/AndroidManifest.xml` and using a unique name for the paths XML file (`debug_provider_paths.xml`).
3. Update `DebugLogsActivity.kt` to use the new unique FileProvider authority.
