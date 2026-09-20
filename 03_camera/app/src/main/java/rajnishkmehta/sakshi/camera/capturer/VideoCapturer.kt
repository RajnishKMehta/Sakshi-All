package rajnishkmehta.sakshi.camera.capturer

import rajnishkmehta.sakshi.camera.debug.DebugLogger as Log
import android.Manifest
import android.animation.ValueAnimator
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.location.Location
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import android.view.View
import android.webkit.MimeTypeMap
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import rajnishkmehta.sakshi.camera.App
import rajnishkmehta.sakshi.camera.CamConfig
import rajnishkmehta.sakshi.camera.CapturedItem
import rajnishkmehta.sakshi.camera.ITEM_TYPE_VIDEO
import rajnishkmehta.sakshi.camera.R
import rajnishkmehta.sakshi.camera.VIDEO_NAME_PREFIX
import rajnishkmehta.sakshi.camera.ui.activities.MainActivity
import rajnishkmehta.sakshi.camera.ui.activities.SecureMainActivity
import rajnishkmehta.sakshi.camera.ui.activities.VideoCaptureActivity
import rajnishkmehta.sakshi.camera.ui.showCustomMessageDialog
import rajnishkmehta.sakshi.camera.util.formatVideoDuration
import rajnishkmehta.sakshi.camera.util.getTreeDocumentUri
import rajnishkmehta.sakshi.camera.util.removePendingFlagFromUri
import java.text.SimpleDateFormat
import androidx.lifecycle.lifecycleScope
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.launch

/**
 * Handles the logic for capturing videos, including setting up the [Recorder],
 * managing the recording state (start, stop, pause, mute), and saving the resulting file.
 * Integrates with Sakshi SDK for unified AV sync processing.
 *
 * @property mActivity The main activity instance holding the camera UI and lifecycle.
 */
class VideoCapturer(private val mActivity: MainActivity) {

    val camConfig = mActivity.camConfig

    var isRecording = false
        private set

    private var currentFileId: String? = null

    private var lastMissingOutputUri: android.net.Uri? = null

    private val videoFileFormat = ".mp4"

    private var recording: Recording? = null

    // Invoking this abandons a start still queued behind the record-start sound.
    private var cancelDeferredStart: (() -> Unit)? = null

    var isMuted = false
        private set

    var includeAudio: Boolean = false

    var isPaused = false
        set(value) {
            if (isRecording) {
                if (value) {
                    recording?.pause()
                    mActivity.setFlipCameraIcon(R.drawable.play, R.string.resume_recording)
                    currentFileId?.let { id ->
                        mActivity.lifecycleScope.launch {
                            mActivity.sakshiClient.pauseAVSync(id)
                        }
                    }
                } else {
                    recording?.resume()
                    mActivity.setFlipCameraIcon(R.drawable.pause, R.string.pause_recording)
                    currentFileId?.let { id ->
                        mActivity.lifecycleScope.launch {
                            mActivity.sakshiClient.resumeAVSync(id)
                        }
                    }
                }
            }
            field = value
        }

    private val handler = Handler(Looper.getMainLooper())

    private fun updateTimerTime(timeInNanos: Long) {
        mActivity.timerView.text = formatVideoDuration(timeInNanos / 1_000_000_000)
    }

    private class RecordingContext(
        val pendingRecording: PendingRecording,
        val uri: Uri,
        val fileDescriptor: ParcelFileDescriptor,
        val shouldAddToGallery: Boolean,
        val isPendingMediaStoreUri: Boolean,
        val mimeType: String,
    )

    private fun createRecordingContext(recorder: Recorder, fileName: String): RecordingContext? {
        val mimeType =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(videoFileFormat.removePrefix("."))

        val ctx = mActivity
        val contentResolver = ctx.contentResolver

        val uri: Uri?

        var resolvedMimeType = mimeType ?: "video/mp4"

        var shouldAddToGallery = true
        var isPendingMediaStoreUri = false

        if (ctx is VideoCaptureActivity && ctx.isOutputUriAvailable()) {
            uri = ctx.outputUri
            shouldAddToGallery = false

            resolvedMimeType = uri?.let {
                contentResolver.getType(it)
            } ?: resolvedMimeType
        } else {
            val storageLocation = camConfig.storageLocation

            if (storageLocation == CamConfig.SettingValues.Default.STORAGE_LOCATION) {
                val contentValues = ContentValues().apply {
                    put(MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaColumns.MIME_TYPE, resolvedMimeType)
                    put(MediaColumns.RELATIVE_PATH, DEFAULT_MEDIA_STORE_CAPTURE_PATH)
                    put(MediaColumns.IS_PENDING, 0)
                }
                uri = contentResolver.insert(CamConfig.videoCollectionUri, contentValues)
                isPendingMediaStoreUri = true
            } else {
                val treeUri = Uri.parse(storageLocation)
                val treeDocumentUri = getTreeDocumentUri(treeUri)

                uri = DocumentsContract.createDocument(contentResolver, treeDocumentUri, resolvedMimeType, fileName)
            }
        }

        if (uri == null) {
            return null
        }

        var location: Location? = null
        if (camConfig.requireLocation) {
            location = (mActivity.applicationContext as App).getLocation()
            if (location == null) {
                mActivity.showMessage(R.string.location_unavailable)
            }
        }
        contentResolver.openFileDescriptor(uri,"w")?.let {
            val outputOptions = FileDescriptorOutputOptions.Builder(it)
                .setLocation(location)
                .build()
            val pendingRecording = recorder.prepareRecording(ctx, outputOptions)
            return RecordingContext(pendingRecording, uri, it, shouldAddToGallery, isPendingMediaStoreUri, resolvedMimeType)
        }
        return null
    }

    /**
     * Initiates the video recording process. Configures the recorder, creates the recording context,
     * sets up the pending recording with audio and location (if enabled), and starts the recording.
     * Also handles integration with SakshiClient for AV sync when starting a new recording.
     */
    fun startRecording() {
        Log.d("VideoCapturer", "startRecording() called")
        if (camConfig.camera == null) return
        val recorder = camConfig.videoCapture?.output ?: return
        if (isRecording) return
        isRecording = true

        val dateString = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = VIDEO_NAME_PREFIX + dateString + videoFileFormat

        includeAudio = false

        val ctx = mActivity

        if (ctx.settingsDialog.includeAudioToggle.isChecked) {
            if (ctx.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED) {
                includeAudio = true
            } else {
                ctx.restartRecordingWithMicPermission()
                isRecording = false
                return
            }
        }

        val recordingCtx = try {
            createRecordingContext(recorder, fileName)!!
        } catch (exception: Exception) {
            val foreignUri = ctx is VideoCaptureActivity && ctx.isOutputUriAvailable()
            if (!foreignUri) {
                camConfig.onStorageLocationNotFound()
            }
            ctx.showMessage(R.string.unable_to_access_output_file)
            isRecording = false
            return
        }

        val pendingRecording = recordingCtx.pendingRecording

        if (includeAudio) {
            pendingRecording.withAudioEnabled()
        }

        beforeRecordingStarts()

        // The sound callback may fire more than once; a second PendingRecording.start() throws.
        var consumed = false

        cancelDeferredStart = {
            consumed = true
            cancelDeferredStart = null
            try {
                recordingCtx.fileDescriptor.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            discardUnusedOutput(recordingCtx)
            afterRecordingStops()
        }

        camConfig.mPlayer.playVRStartSound(handler) {
            if (consumed) {
                return@playVRStartSound
            }

            consumed = true
            cancelDeferredStart = null

            var videoSyncStarted = false
            var fileId: String? = null

            recording = pendingRecording.start(ctx.mainExecutor) { event ->
                Log.d("VideoCapturer", "VideoRecordEvent received: ${event.javaClass.simpleName}")

                if (event is VideoRecordEvent.Start) {
                    Log.d("VideoCapturer", "Event: Start")
                    onRecordingStart()
                } else if (event is androidx.camera.video.VideoRecordEvent.Status) {
                    Log.d("VideoCapturer", "Event: Status, bytes: ${event.recordingStats.numBytesRecorded}, time: ${event.recordingStats.recordedDurationNanos}")
                    updateTimerTime(event.recordingStats.recordedDurationNanos)
                    if (!videoSyncStarted && event.recordingStats.numBytesRecorded > 1024) {
                        videoSyncStarted = true
                        val uniqueHash = java.util.UUID.randomUUID().toString().substring(0, 8)
                        fileId = "vid_${uniqueHash}"
                        currentFileId = fileId
                        val avSyncRequest = rajnishkmehta.sakshi.sdk.api.models.AVSyncRequest(
                            fileId = fileId!!,
                            uri = recordingCtx.uri,
                            mediaType = "VIDEO",
                            fileExtension = videoFileFormat.removePrefix(".")
                        )
                        ctx.grantUriPermission(camConfig.vaultPackage, recordingCtx.uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        if (ctx is rajnishkmehta.sakshi.camera.ui.activities.MainActivity) {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                Log.d("VideoCapturer", "Calling SakshiClient.startAVSync with fileId: $fileId")
                                ctx.sakshiClient.startAVSync(avSyncRequest).collect { result ->
                                    if (result is rajnishkmehta.sakshi.sdk.api.SakshiResult.Failure) {
                                        Log.e("SakshiSDK", "Video ingestion failed: " + result.error.message)
                                    }
                                }
                            }
                        }
                    }
                } else if (event is androidx.camera.video.VideoRecordEvent.Finalize) {
                    Log.d("VideoCapturer", "Event: Finalize, error: ${event.error}, cause: ${event.cause}")

                    val outputExists = try {
                        mActivity.contentResolver.openFileDescriptor(recordingCtx.uri, "r")?.use { true } ?: false
                    } catch (e: Exception) {
                        false
                    }

                    if (outputExists) {
                        if (recordingCtx.isPendingMediaStoreUri) {
                            try {
                                // Remove pending flag
                                rajnishkmehta.sakshi.camera.util.removePendingFlagFromUri(mActivity.contentResolver, recordingCtx.uri)
                            } catch (e: Exception) {
                                Log.e("VideoCapturer", "Failed to remove IS_PENDING", e)
                            }
                        }

                        if (videoSyncStarted && fileId != null) {
                            if (ctx is rajnishkmehta.sakshi.camera.ui.activities.MainActivity) {
                                ctx.handleCopyDone(fileId!!)
                            }
                        }
                    } else {
                        Log.e("VideoCapturer", "Recording output is missing/deleted: ${recordingCtx.uri}")
                        if (lastMissingOutputUri != recordingCtx.uri) {
                            lastMissingOutputUri = recordingCtx.uri
                            mActivity.showCustomMessageDialog(R.drawable.ic_error, mActivity.getString(R.string.video_deleted_while_recording)) {
                                mActivity.camConfig.cameraProvider?.unbindAll()
                                mActivity.previewView.keepScreenOn = false
                                mActivity.camConfig.startCamera(true)
                            }
                        }
                    }

                    currentFileId = null
                    afterRecordingStops()
                }
            }

            // The Recording didn't exist yet when the mute/pause setters ran.
            if (isMuted) {
                recording?.mute(true)
            }
            if (isPaused) {
                recording?.pause()
            }

            try {
                // FileDescriptorOutputOptions doc says that the file descriptor should be closed by the
                // caller, and that it's safe to do so as soon as pendingRecording.start() returns
                recordingCtx.fileDescriptor.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val dp16 = 16 * mActivity.resources.displayMetrics.density
    private val dp8 = 8 * mActivity.resources.displayMetrics.density

    // Skinned devices wrap the capture button shape in selectors and layer-lists
    private fun findGradientDrawable(drawable: Drawable?): GradientDrawable? {
        return when (drawable) {
            is GradientDrawable -> drawable
            is StateListDrawable -> findGradientDrawable(drawable.current)
            is LayerDrawable -> {
                (0 until drawable.numberOfLayers)
                    .firstNotNullOfOrNull { findGradientDrawable(drawable.getDrawable(it)) }
            }
            else -> null
        }
    }

    // If no shape can be dug out, skip the cosmetic animation rather than crash
    private fun animateCaptureButtonCorners(from: Float, to: Float) {
        val gd = findGradientDrawable(mActivity.captureButton.drawable) ?: return

        val animator = ValueAnimator.ofFloat(from, to)
        animator.setDuration(300)
            .addUpdateListener { animation ->
                gd.cornerRadius = animation.animatedValue as Float
            }
        animator.start()
    }

    private fun beforeRecordingStarts() {
        // Don't leak paused/muted state from the previous recording into this one.
        isPaused = false
        isMuted = false

        mActivity.previewView.keepScreenOn = true
    }

    private fun onRecordingStart() {
        // TODO: Uncomment this once the main indicator UI gets implemented
        // mActivity.micOffIcon.visibility = View.GONE

        animateCaptureButtonCorners(dp16, dp8)

        mActivity.settingsDialog.videoQualitySpinner.isEnabled = false
        mActivity.settingsDialog.enableEISToggle.isEnabled = false

        // The user may have paused before the recording actually started.
        if (isPaused) {
            mActivity.setFlipCameraIcon(R.drawable.play, R.string.resume_recording)
        } else {
            mActivity.setFlipCameraIcon(R.drawable.pause, R.string.pause_recording)
        }
        mActivity.cancelButtonView.visibility = View.GONE

        // Only the description changes: the drawable stays the same one the corner-radius
        // animation above is holding on to, and replacing it would cut that animation short.
        mActivity.captureButton.contentDescription = mActivity.getString(R.string.stop_recording)

        if (mActivity.requiresVideoModeOnly) {
            mActivity.thirdOption.visibility = View.INVISIBLE
        }

        mActivity.settingsDialog.waitForFocusLockSwitch.isEnabled = false

        // While recording, the gallery button turns into a shutter for stills
        mActivity.setThirdCircleIcon(R.drawable.camera_shutter, R.string.capture)
        mActivity.tabLayout.visibility = View.INVISIBLE
        mActivity.timerView.setText(R.string.start_value_timer)
        mActivity.timerView.visibility = View.VISIBLE

        mActivity.settingsDialog.includeAudioToggle.isEnabled = false

        if (camConfig.includeAudio) {
            mActivity.setMuteToggleState(muted = isMuted)
            mActivity.muteToggle.visibility = View.VISIBLE
        }
    }

    private fun afterRecordingStops() {
        animateCaptureButtonCorners(dp8, dp16)

        mActivity.timerView.visibility = View.GONE
        mActivity.setFlipCameraIcon(R.drawable.flip_camera, R.string.flip_camera)
        mActivity.captureButton.contentDescription =
            mActivity.getString(R.string.start_recording)

        mActivity.settingsDialog.videoQualitySpinner.isEnabled = true
        mActivity.settingsDialog.enableEISToggle.isEnabled = true

        if (mActivity !is VideoCaptureActivity) {
            mActivity.thirdOption.visibility = View.VISIBLE
        }

        if (!mActivity.requiresVideoModeOnly) {
            mActivity.settingsDialog.waitForFocusLockSwitch.isEnabled = true
        }

        // Always restore the third-circle icon and its accessibility label to the gallery button:
        // at record start it was repurposed into an in-video shutter ("Capture") unconditionally,
        // so restoring it only for non-VideoCaptureActivity would strand a stale "Capture" label
        // there. The non-recording third-circle click opens the gallery in every activity, so
        // open_gallery is the accurate label. Cancel/tab visibility stays guarded, as those don't
        // apply to VideoCaptureActivity.
        mActivity.setThirdCircleIcon(R.drawable.option_circle, R.string.open_gallery)

        if (mActivity !is VideoCaptureActivity) {
            mActivity.cancelButtonView.visibility = View.VISIBLE
            mActivity.tabLayout.visibility = View.VISIBLE
        }

        mActivity.previewView.keepScreenOn = false

        // TODO: Uncomment this once the main indicator UI gets implemented
        // if (!mActivity.config.includeAudio)
        //   mActivity.micOffIcon.visibility = View.VISIBLE

        mActivity.settingsDialog.includeAudioToggle.isEnabled = true
        mActivity.muteToggle.visibility = View.GONE

        isRecording = false

        mActivity.forceUpdateOrientationSensor()
    }

    /**
     * Mutes the currently active recording. Does nothing if not recording or audio is disabled.
     */
    fun muteRecording() {
        if (!isRecording) return
        check(camConfig.includeAudio)
        isMuted = true
        recording?.mute(true)
    }

    /**
     * Unmutes the currently active recording. Does nothing if not recording or audio is disabled.
     */
    fun unmuteRecording() {
        if (!isRecording) return
        check(camConfig.includeAudio)
        isMuted = false
        recording?.mute(false)
    }

    /**
     * Stops the currently active recording and releases its resources.
     * Also invokes any pending deferred start actions if a stop was requested immediately after a start.
     */
    fun stopRecording() {
        Log.d("VideoCapturer", "stopRecording() called")
        cancelDeferredStart?.let {
            it()
            return
        }

        recording?.stop()
        recording?.close()
        recording = null
    }

    private fun discardUnusedOutput(recordingCtx: RecordingContext) {
        if (!recordingCtx.shouldAddToGallery) {
            return
        }

        try {
            if (recordingCtx.isPendingMediaStoreUri) {
                mActivity.contentResolver.delete(recordingCtx.uri, null, null)
            } else {
                DocumentsContract.deleteDocument(mActivity.contentResolver, recordingCtx.uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private const val STALE_PENDING_RECORDING_AGE = 60 * 60 * 1000L

// A recording that dies with its process (swipe-away from Recents, OOM kill, crash) never reaches
// the Finalize callback that clears IS_PENDING, so it leaves a half-written file that is invisible
// to the user until MediaProvider expires it a week later, and unplayable in the meantime since it
// has no moov atom. Deleting is the honest outcome. Pending rows are only visible to the app that
// owns them, so this can never reach another app's in-flight write, and the age cutoff keeps it
/**
 * Deletes stale pending recording entries from the MediaStore that are older than the specified maximum age.
 * This cleans up incomplete files left behind if the application process dies unexpectedly before finalizing them.
 *
 * @param context The application context used for content resolver operations.
 * @param maxAge The maximum age in milliseconds before a pending recording is considered stale.
 */
// A recording that dies with its process (swipe-away from Recents, OOM kill, crash) never reaches
// the Finalize callback that clears IS_PENDING, so it leaves a half-written file that is invisible
// to the user until MediaProvider expires it a week later, and unplayable in the meantime since it
// has no moov atom. Deleting is the honest outcome. Pending rows are only visible to the app that
// owns them, so this can never reach another app's in-flight write, and the age cutoff keeps it
// clear of a recording that is still being muxed.
fun deleteStalePendingRecordings(
    context: Context,
    maxAge: Long = STALE_PENDING_RECORDING_AGE,
) {
    val selection = "${MediaColumns.IS_PENDING} = 1" +
            " AND ${MediaColumns.DISPLAY_NAME} LIKE ?" +
            " AND ${MediaColumns.DATE_ADDED} < ?"
    val cutoffSeconds = (System.currentTimeMillis() - maxAge) / 1000L
    val args = arrayOf("$VIDEO_NAME_PREFIX%", cutoffSeconds.toString())

    try {
        // Pending rows are filtered out of every operation unless they are explicitly asked for.
        @Suppress("DEPRECATION")
        val collection = MediaStore.setIncludePending(CamConfig.videoCollectionUri)
        context.contentResolver.delete(collection, selection, args)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Retrieves a thumbnail bitmap from the video file at the given URI.
 *
 * @param context The context for setting the data source.
 * @param uri The URI of the video file.
 * @return The thumbnail bitmap, or null if it could not be generated.
 */
@Throws(Exception::class)
fun getVideoThumbnail(context: Context, uri: Uri?): Bitmap? {
    MediaMetadataRetriever().use {
        it.setDataSource(context, uri)
        return it.frameAtTime
    }
}
