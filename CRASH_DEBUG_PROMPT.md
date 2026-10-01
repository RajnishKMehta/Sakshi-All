# Problem Description

We are experiencing a persistent app crash in the Sākṣī Portal application (`:04_portal`) on Android. The crash occurs immediately upon navigating to the `VaultSelectionScreen`.

## The Setup
The `VaultSelectionScreen` is a Jetpack Compose screen. It queries the device's `PackageManager` for installed apps via a `VaultSelectionViewModel` and displays them in a `LazyColumn`.

We recently made the following changes:
1. Replaced a hardcoded placeholder icon (`android.R.drawable.sym_def_app_icon`) with the actual application icon (`Drawable`) fetched from the `PackageManager`.
2. Fixed back navigation logic in `MainActivity` (using a `List<Screen>` stack).
3. Reset the search query when opening the screen using a `LaunchedEffect`.
4. Added a "Download Vault" action in the `TopAppBar`.

## The Crash
Despite these fixes, and despite adding defensive `try-catch` blocks around the `Drawable.toBitmap()` conversion in the `AppItem` composable, the app continues to crash **the moment the user navigates to the `VaultSelectionScreen`**.

There is no stack trace outputted to the standard logcat that points to our code. The only logs we see are:

```log
2026-10-01 09:19:33.714 [INFO] GalleryScreen: Loading gallery items
2026-10-01 09:19:33.744 [INFO] MainActivity: First launch, pinging default vault: rajnishkmehta.sakshi.vault
2026-10-01 09:19:34.022 [INFO] MainActivity: Default vault ping success, setting as default
2026-10-01 09:19:41.379 [INFO] VaultSelectionViewModel: Loading apps...
2026-10-01 09:20:09.741 [INFO] GalleryScreen: Loading gallery items
2026-10-01 09:20:09.806 [INFO] MainActivity: Pinging saved vault: rajnishkmehta.sakshi.vault
2026-10-01 09:20:09.863 [INFO] MainActivity: Ping success
```
Notice that the "VaultSelectionScreen opened" log (which is placed inside a `LaunchedEffect` in `VaultSelectionScreen`) **never fires**. The app just silently dies right after "Loading apps..." is logged by the ViewModel.

## File Context
The relevant files and their current state are provided in the repository (specifically `04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/vault/VaultSelectionScreen.kt`, `MainActivity.kt`, and `AppDiscoveryRepository.kt`).

The `AppDiscoveryRepository` fetches all installed apps using `PackageManager.getInstalledApplications`.

## Task for AI
As an expert Android and Jetpack Compose developer, please analyze this scenario. Since there is no explicit stack trace, look for silent killers.
1. What could cause a silent crash or immediate death when entering a Jetpack Compose screen that queries installed packages?
2. Consider things like memory limits (fetching hundreds of `Drawable` icons simultaneously in the ViewModel/Repository), `TransactionTooLargeException`, IPC limits, or Main Thread blocking.
3. Review the `AppDiscoveryRepository.kt` where `pm.getApplicationIcon(appInfo)` is called for *every* app on the device and stored in a list. Could holding all these `Drawable`s in memory at once be causing an OutOfMemoryError (OOM) that kills the process without a proper crash log?
4. Provide a detailed explanation of the likely root cause and provide the exact code changes needed to fix it.
