cat << 'PATCH' > all_docs.patch
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/data/SettingsRepository.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/data/SettingsRepository.kt
@@ -11,26 +11,62 @@
 import kotlinx.coroutines.flow.Flow
 import kotlinx.coroutines.flow.map

+/**
+ * Extension property providing a singleton instance of the DataStore.
+ */
 val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

+/**
+ * Repository for managing application settings via Android DataStore.
+ *
+ * Handles persistence of user preferences, such as dynamic color settings
+ * and the selected target Vault application package for IPC communication.
+ *
+ * @property context The application context used to access the DataStore.
+ */
 class SettingsRepository(private val context: Context) {

     companion object {
+        /**
+         * Key for storing the dynamic color preference.
+         */
         val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
+
+        /**
+         * Key for storing the selected Vault application package name.
+         */
         val VAULT_PACKAGE_KEY = stringPreferencesKey("vault_package")
     }

+    /**
+     * A [Flow] emitting the current state of the dynamic color setting.
+     * Defaults to true (enabled) if the preference is not set.
+     */
     val useDynamicColorFlow: Flow<Boolean> = context.dataStore.data
         .map { preferences ->
             // By default, Material You is enabled
             preferences[DYNAMIC_COLOR_KEY] ?: true
         }

+    /**
+     * A [Flow] emitting the currently selected Vault package name,
+     * or null if no Vault app has been selected.
+     */
     val vaultPackageFlow: Flow<String?> = context.dataStore.data
         .map { preferences ->
             preferences[VAULT_PACKAGE_KEY]
         }

+    /**
+     * Updates the dynamic color setting.
+     *
+     * @param useDynamicColor Whether dynamic color (Material You) should be enabled.
+     */
     suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
         context.dataStore.edit { preferences ->
             preferences[DYNAMIC_COLOR_KEY] = useDynamicColor
         }
     }

+    /**
+     * Updates the selected Vault package name.
+     *
+     * @param packageName The package name of the Vault app to connect to, or null to clear the selection.
+     */
     suspend fun setVaultPackage(packageName: String?) {
         context.dataStore.edit { preferences ->
             if (packageName == null) {
@@ -41,6 +77,11 @@
         }
     }

+    /**
+     * Sets the Vault package name only if it has not been previously set.
+     *
+     * @param packageName The default package name of the Vault app to use.
+     */
     suspend fun setVaultPackageIfUnset(packageName: String) {
         context.dataStore.edit { preferences ->
             if (!preferences.contains(VAULT_PACKAGE_KEY)) {
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/data/deletion/MediaDeletionRepository.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/data/deletion/MediaDeletionRepository.kt
@@ -40,7 +40,14 @@
     private val removedMediaStore: RemovedMediaStore
 ) {

-    /** Schedules [fileId] for deletion. Never reports success before Vault confirmed it. */
+    /**
+     * Schedules [fileId] for deletion via the Vault application.
+     *
+     * Suspends until the operation completes or fails. Never reports success before Vault has confirmed it.
+     *
+     * @param fileId The unique identifier of the file to schedule for deletion.
+     * @return [ScheduleDeletionResult.Scheduled] if successful, [ScheduleDeletionResult.Failed] otherwise.
+     */
     suspend fun scheduleDeletion(fileId: String): ScheduleDeletionResult {
         val result = try {
             deleteMedia(fileId)
@@ -58,7 +65,14 @@
         }
     }

-    /** Recovers [fileId] from scheduled deletion. */
+    /**
+     * Recovers [fileId] from a scheduled deletion state via the Vault application.
+     *
+     * Suspends until the operation completes or fails.
+     *
+     * @param fileId The unique identifier of the file to recover.
+     * @return [RecoverMediaResult.Recovered] if successful, [RecoverMediaResult.Failed] otherwise.
+     */
     suspend fun recoverMediaAction(fileId: String): RecoverMediaResult {
         val result = try {
             recoverMedia(fileId)
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/data/export/MediaExporter.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/data/export/MediaExporter.kt
@@ -40,6 +40,7 @@
      * (see [ExportCollection.forMedia]). Existing files are never overwritten.
      *
      * @throws CancellationException if the calling coroutine is cancelled; the partial file is removed first.
+     * @return A [MediaExportResult] detailing the outcome of the export operation.
      */
     suspend fun export(mediaDetails: MediaDetailsResponse): MediaExportResult = withContext(ioDispatcher) {
         val extension = normalizeExtension(mediaDetails.fileExtension)
@@ -75,6 +76,14 @@
         }
     }

+    /**
+     * Streams the content from the source URI to the destination URI.
+     *
+     * @param source The URI of the source file to read from.
+     * @param destination The URI of the destination file to write to.
+     * @return The total number of bytes copied.
+     * @throws IllegalStateException if either stream cannot be opened.
+     */
     private suspend fun copyInto(source: Uri, destination: Uri): Long {
         val input = contentResolver.openInputStream(source)
             ?: throw IllegalStateException("Unable to open source file")
@@ -86,6 +95,12 @@
         }
     }

+    /**
+     * Queries the actual display name and relative path of the exported file.
+     *
+     * @param uri The MediaStore URI to query.
+     * @return A Pair containing the display name and relative path, or null if the query fails.
+     */
     private fun queryLocation(uri: Uri): Pair<String, String>? = try {
         contentResolver.query(
             uri,
@@ -107,6 +122,11 @@
         null
     }

+    /**
+     * Deletes the specified URI without throwing exceptions. Used for cleanup on failure.
+     *
+     * @param uri The URI to delete.
+     */
     private fun deleteQuietly(uri: Uri) {
         try {
             contentResolver.delete(uri, null, null)
@@ -115,6 +135,11 @@
         }
     }

+    /**
+     * Resolves the corresponding MediaStore volume URI for the target collection.
+     *
+     * @return The volume-specific content URI.
+     */
     private fun ExportCollection.contentUri(): Uri = when (this) {
         ExportCollection.IMAGES -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
         ExportCollection.VIDEO -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/vault/AppInfo.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/vault/AppInfo.kt
@@ -4,6 +4,13 @@
  */
 package rajnishkmehta.sakshi.portal.vault

+/**
+ * Basic information about a Vault application discovered on the device.
+ *
+ * @property name The human-readable application label (e.g. "Sakshi Vault").
+ * @property packageName The application's package name (e.g. "rajnishkmehta.sakshi.vault").
+ * @property hasLauncherActivity True if the application has a launcher intent that can be opened.
+ */
 data class AppInfo(
     val name: String,
     val packageName: String,
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/vault/AppDiscoveryRepository.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/vault/AppDiscoveryRepository.kt
@@ -12,8 +12,19 @@
 import kotlinx.coroutines.withContext
 import rajnishkmehta.sakshi.portal.debug.DebugLogger as Log

+/**
+ * Repository for discovering non-system applications installed on the device.
+ *
+ * Used primarily to populate the Vault selection screen so the user can choose
+ * which Vault app the Portal should connect to via IPC.
+ *
+ * @property context The application context used to query the [PackageManager].
+ */
 class AppDiscoveryRepository(private val context: Context) {

+    /**
+     * Retrieves a list of non-system applications installed on the device.
+     *
+     * The returned list is prioritized: the default Sakshi Vault package is placed first,
+     * followed by background-only apps (which often include custom headless Vaults), and finally
+     * standard user applications sorted alphabetically.
+     *
+     * @return A prioritized list of [AppInfo] representing installed non-system applications.
+     */
     suspend fun getInstalledApplications(): List<AppInfo> = withContext(Dispatchers.IO) {
         // try { Log.i(tag = "VaultSelection", message = "AppDiscoveryRepository: getInstalledApplications started") } catch (e: Exception) {}
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/vault/VaultSelectionViewModel.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/vault/VaultSelectionViewModel.kt
@@ -19,6 +19,13 @@
 import rajnishkmehta.sakshi.portal.R
 import kotlinx.coroutines.CancellationException

+/**
+ * ViewModel for discovering and selecting a compatible Vault application.
+ *
+ * Manages loading the list of installed non-system apps, filtering them by user search query,
+ * and actively verifying compatibility via a test IPC connection before confirming
+ * the selection.
+ */
 class VaultSelectionViewModel(application: Application) : AndroidViewModel(application) {

     private val repository = AppDiscoveryRepository(application)
@@ -35,6 +42,10 @@
         loadApps()
     }

+    /**
+     * Asynchronously loads the list of installed non-system applications and updates [uiState].
+     * Enters [UiState.Error] if loading fails.
+     */
     private fun loadApps() {
         viewModelScope.launch {
             _uiState.value = UiState.Loading
@@ -52,6 +63,11 @@
         }
     }

+    /**
+     * Filters the currently loaded apps based on the provided search query.
+     *
+     * @param query The search text applied to app names and package names (case-insensitive).
+     */
     fun filter(query: String) {
         val currentState = _uiState.value
         if (currentState is UiState.Success || currentState is UiState.Filtering) {
@@ -67,11 +83,23 @@
         }
     }

+    /**
+     * Cancels any ongoing Vault verification request.
+     */
     fun cancelVerification() {
         verificationJob?.cancel()
         verificationJob = null
     }

+    /**
+     * Initiates a ping request to the specified application via the Sakshi SDK to verify
+     * it is a compatible Vault instance.
+     *
+     * Cancels any previously ongoing verification before starting.
+     *
+     * @param packageName The package name to verify.
+     * @param onResult Callback invoked with `true` if verified, or `false` and an error message on failure.
+     */
     fun verifyVaultApp(packageName: String, onResult: (Boolean, String?) -> Unit) {
         cancelVerification()
         verificationJob = viewModelScope.launch {
@@ -101,9 +129,20 @@
         }
     }

+    /**
+     * Represents the UI state for the Vault selection screen.
+     */
     sealed class UiState {
-        object Loading : UiState()
+        /** Initial state while applications are being discovered. */
+        data object Loading : UiState()
+
+        /** Successfully loaded the complete list of [apps]. */
         data class Success(val apps: List<AppInfo>) : UiState()
+
+        /** Displaying a filtered list of [apps] based on a search query. */
         data class Filtering(val apps: List<AppInfo>) : UiState()
+
+        /** Failed to load applications. */
         data class Error(val message: String) : UiState()
     }
 }
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/gallery/MediaType.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/gallery/MediaType.kt
@@ -4,5 +4,9 @@
  */
 package rajnishkmehta.sakshi.portal.ui.gallery

+/**
+ * Filter categories for displaying media items in the gallery.
+ * Matches Vault media types.
+ */
 enum class MediaType {
     ALL, PHOTO, VIDEO, AUDIO, OTHER
 }
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/gallery/GalleryUiState.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/gallery/GalleryUiState.kt
@@ -4,13 +4,28 @@
  */
 package rajnishkmehta.sakshi.portal.ui.gallery

+/**
+ * Represents the current state of the gallery UI.
+ */
 sealed interface GalleryUiState {
+    /** Initial state while fetching media from the Vault. */
     data object Loading : GalleryUiState
+
+    /** Successfully retrieved the list of media items. */
     data class Success(val media: List<ParsedMediaItem>) : GalleryUiState
+
+    /** An error occurred while fetching media. */
     data class Error(val message: String) : GalleryUiState
+
+    /** The configured Vault package is not available or disconnected. */
     data object VaultUnavailable : GalleryUiState
 }

 /**
  * Returns [state] without the media whose IDs are in [removedIds]. Non-success states are returned unchanged.
+ *
+ * Used to immediately hide items scheduled for deletion from the UI before the next full refresh.
+ *
+ * @param state The current UI state.
+ * @param removedIds Set of IDs to filter out.
  */
 fun excludeRemovedMedia(state: GalleryUiState, removedIds: Set<String>): GalleryUiState =
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/gallery/GalleryViewModel.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/gallery/GalleryViewModel.kt
@@ -29,12 +29,21 @@
 import kotlinx.coroutines.CancellationException
 import kotlinx.coroutines.cancelAndJoin

+/**
+ * Represents a single raw media item entry as returned by the Vault IPC list response.
+ *
+ * @property fileId The unique identifier of the file.
+ * @property timestamp The creation or modification timestamp of the file in milliseconds.
+ */
 @Serializable
 data class RawMediaItem(
     val fileId: String,
     val timestamp: Long
 )

+/**
+ * Represents a fully parsed and structured media item for display in the Gallery UI.
+ */
 data class ParsedMediaItem(
     val id: String,
     val type: MediaType,
@@ -43,15 +52,26 @@
 )

 /**
- * ViewModel for the gallery: lists media from Vault and hides media that Vault confirmed as
- * scheduled for deletion.
+ * ViewModel managing the state of the Gallery screen.
+ *
+ * Responsible for fetching the list of available media from the configured Vault app via IPC,
+ * tracking loading/refresh states, and immediately hiding media that has been confirmed as
+ * scheduled for deletion by the Vault.
  *
  * @param removedMediaStore Source of media IDs to hide immediately.
  * @param isDeletedMode True if this viewModel is for the Scheduled Deletions screen.
  */
 class GalleryViewModel(
     application: Application,
     private val settingsRepository: SettingsRepository,
     private val removedMediaStore: RemovedMediaStore,
     private val isDeletedMode: Boolean = false
 ) : AndroidViewModel(application) {

+    /** Internal mutable state representing the gallery UI. */
     private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
+
+    /**
+     * Public exposed state of the gallery UI.
+     * Automatically filters out items present in the [removedMediaStore].
+     */
     val uiState: StateFlow<GalleryUiState> = combine(_uiState, removedMediaStore.removedIds, ::excludeRemovedMedia)
         .stateIn(viewModelScope, SharingStarted.Eagerly, GalleryUiState.Loading)

@@ -79,12 +99,21 @@
         }
     }

+    /**
+     * Initiates a manual refresh of the media list (e.g. from pull-to-refresh).
+     */
     fun refreshMedia() {
         _isRefreshing.value = true
         loadMedia(isRefresh = true)
     }

     /**
-     * Loads the media list from Vault. Every load (app start, Vault switch, retry, pull-to-refresh)
-     * first clears [removedMediaStore]; the currently shown list keeps those items hidden until the
-     * fresh Vault result replaces it.
+     * Loads the media list from the Vault via IPC.
+     *
+     * Every load (app start, Vault switch, retry, pull-to-refresh) first clears the
+     * [removedMediaStore]. The currently shown list keeps those items hidden until the
+     * fresh Vault result replaces it.
+     *
+     * @param specificPackage An optional specific package name to connect to, overriding the flow temporarily.
+     * @param isRefresh True if this load is a silent refresh rather than a full page load.
      */
     fun loadMedia(specificPackage: String? = null, isRefresh: Boolean = false) {
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/gallery/GalleryScreen.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/gallery/GalleryScreen.kt
@@ -82,6 +82,8 @@
  * The main gallery screen that displays media from the Vault.
  *
+ * @param modifier The modifier to be applied to the layout.
+ * @param settingsRepository Repository for retrieving app settings like the selected Vault package.
  * @param removedMediaStore Store containing media items scheduled for deletion. Must be the store shared with the viewer so its lifetime covers both screens.
+ * @param onSettingsClick Callback invoked when the user navigates to the settings screen.
  * @param isDeletedMode When true, the gallery displays only media scheduled for deletion.
  * @param onScheduledDeletionsClick Callback invoked when the user navigates to the scheduled deletions screen.
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/ScheduleDeletionDialog.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/ScheduleDeletionDialog.kt
@@ -19,6 +19,9 @@
  *
  * Explains that the file is not deleted immediately but permanently deleted by Vault after the
  * retention period. Dismissing the dialog leaves the file unchanged.
+ *
+ * @param onConfirm Callback invoked when the user confirms they want to schedule deletion.
+ * @param onDismiss Callback invoked when the dialog is dismissed or canceled.
  */
 @Composable
 fun ScheduleDeletionDialog(
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/MediaViewerViewModel.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/MediaViewerViewModel.kt
@@ -23,9 +23,17 @@
 import kotlinx.coroutines.flow.firstOrNull
 import rajnishkmehta.sakshi.portal.debug.DebugLogger as Log

+/**
+ * Represents the UI state of the media viewer screen.
+ */
 sealed interface MediaViewerUiState {
+    /** Media details are currently being loaded from the Vault. */
     data object Loading : MediaViewerUiState
+
+    /** Successfully retrieved [mediaDetails] from the Vault. */
     data class Success(val mediaDetails: MediaDetailsResponse) : MediaViewerUiState
+
+    /** An error occurred while fetching media details. */
     data class Error(val message: String) : MediaViewerUiState
 }

@@ -34,6 +42,8 @@
  * schedules it for deletion.
  *
+ * @param application The Application context.
+ * @param settingsRepository Repository for retrieving the configured Vault package.
  * @param removedMediaStore Store notified when Vault confirmed a scheduled deletion.
  * @param mediaExporter Exporter used by [exportMedia] to save the file into shared storage.
  */
@@ -90,6 +100,11 @@
         removedMediaStore = removedMediaStore
     )

+    /**
+     * Loads the full media details for the specified [fileId] from the Vault.
+     *
+     * @param fileId The unique identifier of the media to load.
+     */
     fun loadMedia(fileId: String) {
         viewModelScope.launch {
             _uiState.value = MediaViewerUiState.Loading
@@ -118,6 +133,10 @@
         }
     }

+    /**
+     * Initiates the export (Save/Download) of the current media file to shared storage.
+     * Updates [exportState] to reflect progress and the final outcome.
+     */
     fun exportMedia() {
         val currentState = _uiState.value
         if (currentState !is MediaViewerUiState.Success) return
@@ -131,10 +150,16 @@
         }
     }

+    /** Resets the export UI state to [MediaExportUiState.Idle]. */
     fun clearExportResult() {
         _exportState.value = MediaExportUiState.Idle
     }

+    /**
+     * Starts the confirmation flow for scheduling deletion.
+     *
+     * Sets [deletionState] to [MediaDeletionUiState.Confirming].
+     */
     fun confirmDeletion() {
         val currentState = _uiState.value
         if (currentState !is MediaViewerUiState.Success) return
@@ -144,6 +169,10 @@
         }
     }

+    /**
+     * Proceeds with scheduling the current media for deletion after user confirmation.
+     * Updates [deletionState] to reflect the ongoing operation and its outcome.
+     */
     fun scheduleDeletion() {
         val currentState = _uiState.value
         if (currentState !is MediaViewerUiState.Success) return
@@ -165,10 +194,18 @@
         }
     }

+    /**
+     * Cancels the deletion confirmation flow or resets a completed deletion state back to idle.
+     */
     fun cancelDeletion() {
         _deletionState.value = MediaDeletionUiState.Idle
     }

+    /**
+     * Recovers the current media item that was previously scheduled for deletion.
+     *
+     * Valid only if the file is currently scheduled for deletion.
+     */
     fun recoverMedia() {
         val currentState = _uiState.value
         if (currentState !is MediaViewerUiState.Success) return
@@ -192,10 +229,16 @@
         }
     }

+    /**
+     * Resets the recovery UI state to idle.
+     */
     fun cancelRecover() {
         _recoverState.value = MediaDeletionUiState.Idle
     }

+    /**
+     * Overrides [onCleared] to ensure the [SakshiClient] connection is safely closed.
+     */
     override fun onCleared() {
         super.onCleared()
         sakshiClient?.disconnect()
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/MediaViewerScreen.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/MediaViewerScreen.kt
@@ -52,6 +52,7 @@
  * @param scheduledTime Timestamp when deletion was scheduled, only applicable when isDeletedMode is true.
  * @param settingsRepository Repository for retrieving app settings like the selected Vault package.
  * @param removedMediaStore Store that must be the gallery's shared store to communicate successful deletions.
+ * @param modifier The modifier to be applied to the layout.
  * @param onBackClick Callback invoked to navigate back.
  */
 @Composable
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/SpecificViewers.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/SpecificViewers.kt
@@ -70,6 +70,14 @@


+/**
+ * Displays a photograph using Coil, supporting pinch-to-zoom and panning.
+ *
+ * The image is centered by default. Zooming is constrained between 1x and 5x,
+ * and panning is constrained to the scaled bounds of the image.
+ *
+ * @param uri The URI string pointing to the image to load.
+ */
 @Composable
 fun PhotoViewer(uri: String) {
     var scale by remember { mutableStateOf(1f) }
@@ -107,6 +115,13 @@
     }
 }

+/**
+ * Displays a video or audio file using Media3 ExoPlayer.
+ *
+ * Automatically initializes the player, handles lifecycle transitions (play/pause/release),
+ * and renders the default Player UI with manual control auto-hiding behavior.
+ *
+ * @param uri The URI string pointing to the media stream to play.
+ */
 @Composable
 fun VideoPlayer(uri: String) {
     val context = LocalContext.current
@@ -197,6 +212,12 @@
     }
 }

+/**
+ * Fallback viewer for unrecognized media types or files that cannot be played directly.
+ *
+ * Displays a generic placeholder icon with text indicating the file type is unsupported,
+ * but allows users to still export the raw data.
+ */
 @Composable
 fun OtherViewer() {
     Box(
@@ -232,6 +253,9 @@
     }
 }

+/**
+ * Displays a continuous loading spinner to indicate that media is currently being fetched or prepared.
+ */
 @Composable
 fun LoadingViewer() {
     Box(
@@ -267,6 +291,11 @@
     }
 }

+/**
+ * Displays an error state when a media file fails to load or cannot be retrieved from the Vault.
+ *
+ * @param message The specific error message describing the failure reason.
+ */
 @Composable
 fun ErrorViewer(message: String) {
     Box(
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/InfoBottomSheet.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/InfoBottomSheet.kt
@@ -58,6 +58,7 @@
  * @param mediaDetails The core media properties (name, size, type, location).
  * @param isDeletedMode True if displaying info for a media item scheduled for deletion; affects metadata visibility.
  * @param scheduledTime The timestamp when deletion was scheduled (used only when isDeletedMode is true).
+ * @param sheetState The state to control the bottom sheet's visibility and position.
  * @param onDismissRequest Callback to dismiss the bottom sheet.
  */
 @ExperimentalMaterial3Api
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/settings/SettingsScreen.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/settings/SettingsScreen.kt
@@ -42,6 +42,13 @@
 import rajnishkmehta.sakshi.portal.BuildConfig
 import rajnishkmehta.sakshi.portal.data.SettingsRepository

+/**
+ * Displays the application settings screen.
+ *
+ * @param onVaultSelectionClick Callback invoked to navigate to the Vault application selection screen.
+ * @param repository The [SettingsRepository] used to read and update user preferences.
+ * @param onBackClick Callback invoked when the user navigates back.
+ */
 @OptIn(ExperimentalMaterial3Api::class)
 @Composable
 fun SettingsScreen(
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/MainActivity.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/MainActivity.kt
@@ -29,6 +29,13 @@
 import rajnishkmehta.sakshi.portal.ui.viewer.MediaViewerScreen
 import rajnishkmehta.sakshi.portal.ui.gallery.MediaType

+/**
+ * The main entry point for the Portal application.
+ *
+ * Sets up the Compose navigation graph and provides shared dependencies (like
+ * the [SettingsRepository] and [RemovedMediaStore]) to the top-level screens.
+ * Handles routing between the gallery, media viewer, settings, and Vault selection screens.
+ */
 class MainActivity : ComponentActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
@@ -98,12 +105,21 @@
     }
 }

+/**
+ * Navigation key for the main Gallery screen.
+ */
 @Serializable
 data object GalleryScreenRoute : NavKey

+/**
+ * Navigation key for the Settings screen.
+ */
 @Serializable
 data object SettingsScreenRoute : NavKey

+/**
+ * Navigation key for the Vault application selection screen.
+ */
 @Serializable
 data object VaultSelectionScreenRoute : NavKey

@@ -118,7 +134,8 @@
  *
  * @property fileId The unique identifier of the media file to view.
  * @property mediaType The type of media (e.g., photo, video) for rendering logic.
- * @property isDeletedMode Whether the viewer is opened from the scheduled deletions screen.
+ * @property isDeletedMode True if the viewer is opened from the scheduled deletions screen.
+ * @property scheduledTime The timestamp when deletion was scheduled (used only when isDeletedMode is true).
  */
 @Serializable
 data class MediaViewerRoute(val fileId: String, val mediaType: MediaType, val isDeletedMode: Boolean = false, val scheduledTime: Long = -1L) : NavKey
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/PortalApp.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/PortalApp.kt
@@ -7,6 +7,11 @@
 import android.app.Application
 import rajnishkmehta.sakshi.portal.debug.DebugLogger as Log

+/**
+ * Custom Application class for the Portal app.
+ *
+ * Responsible for initializing application-wide components like the custom logger on startup.
+ */
 class PortalApp : Application() {
     override fun onCreate() {
         super.onCreate()
--- 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/theme/Theme.kt
+++ 04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/theme/Theme.kt
@@ -82,6 +82,15 @@
     scrim = md_theme_dark_scrim,
 )

+/**
+ * Central theme composable for the Portal app.
+ *
+ * Applies Material 3 styling, handles dynamic color on Android 12+, and configures system window edges and status bar appearance.
+ *
+ * @param useDarkTheme Whether to force a dark color scheme. Defaults to the system setting.
+ * @param useDynamicColor Whether to utilize Material You dynamic colors if available (API 31+).
+ * @param content The composable hierarchy to render with this theme.
+ */
 @Composable
 fun PortalTheme(
     useDarkTheme: Boolean = isSystemInDarkTheme(),
PATCH
patch -p0 < all_docs.patch
