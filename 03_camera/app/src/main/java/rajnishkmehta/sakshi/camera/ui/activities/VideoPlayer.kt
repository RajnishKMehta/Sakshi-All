package rajnishkmehta.sakshi.camera.ui.activities

import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import rajnishkmehta.sakshi.camera.debug.DebugLogger as Log
import android.widget.FrameLayout

import android.widget.RelativeLayout
import android.widget.Toast
import androidx.media3.ui.PlayerView
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.common.Player
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import rajnishkmehta.sakshi.camera.AutoFinishOnSleep
import rajnishkmehta.sakshi.camera.R
import rajnishkmehta.sakshi.camera.databinding.VideoPlayerBinding
import rajnishkmehta.sakshi.camera.util.getParcelableExtra
import kotlin.concurrent.thread


@OptIn(UnstableApi::class)
class VideoPlayer : AppCompatActivity() {

    companion object {
        const val TAG = "VideoPlayer"
        const val IN_SECURE_MODE = "isInSecureMode"
        const val VIDEO_URI = "videoUri"
        private const val KEY_PLAY_WHEN_READY = "playWhenReady"
        private const val KEY_PLAYBACK_POSITION = "playbackPosition"
    }

    private lateinit var binding: VideoPlayerBinding

    private val autoFinisher = AutoFinishOnSleep(this)
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var playbackPosition = 0L

    private var isSecureMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            playWhenReady = savedInstanceState.getBoolean(KEY_PLAY_WHEN_READY, true)
            playbackPosition = savedInstanceState.getLong(KEY_PLAYBACK_POSITION, 0L)
        }
        enableEdgeToEdge()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = false
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

        val intent = this.intent

        isSecureMode = intent.getBooleanExtra(IN_SECURE_MODE, false)

        if (isSecureMode) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            autoFinisher.start()
        }

        binding = VideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.let {
            it.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.appbar)))
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayHomeAsUpEnabled(true)
        }

        val uri = getParcelableExtra<Uri>(intent, VIDEO_URI)!!

        val playerView = binding.videoPlayer
        supportActionBar?.setBackgroundDrawable(null)
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)



        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            if (visibility == android.view.View.VISIBLE) {
                showActionBar()
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            } else {
                hideActionBar()
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
        })



        ViewCompat.setOnApplyWindowInsetsListener(binding.shade) { view, insets ->
            val systemBars = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
            val actionBarHeight = resources.getDimensionPixelSize(R.dimen.action_bar_height)
            view.layoutParams =
                FrameLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    systemBars.top + actionBarHeight
                )
            view.background = ContextCompat.getDrawable(this@VideoPlayer, R.drawable.shade)
            insets
        }

    }




    private fun initializePlayer() {
        if (player == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()

            val extractorsFactory = androidx.media3.extractor.DefaultExtractorsFactory()
                .setFragmentedMp4ExtractorFlags(
                    androidx.media3.extractor.mp4.FragmentedMp4Extractor.FLAG_ENABLE_EMSG_TRACK or
                    androidx.media3.extractor.mp4.FragmentedMp4Extractor.FLAG_READ_MFRA_FOR_SEEK_MAP
                )

            val mediaSourceFactory = DefaultMediaSourceFactory(this, extractorsFactory)

            val exoPlayer = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
            exoPlayer.setAudioAttributes(audioAttributes, true)
            binding.videoPlayer.player = exoPlayer
            player = exoPlayer

            val uri = getParcelableExtra<Uri>(intent, VIDEO_URI)!!
            val mediaItem = MediaItem.fromUri(uri)

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.seekTo(playbackPosition)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = playWhenReady

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        showActionBar()
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e(TAG, "Unable to play video", error)
                    Toast.makeText(applicationContext, R.string.unable_to_play_video, Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }


    override fun onStop() {
        super.onStop()
        player?.let {
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition
            it.release()
        }
        player = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        player?.let {
            outState.putBoolean(KEY_PLAY_WHEN_READY, it.playWhenReady)
            outState.putLong(KEY_PLAYBACK_POSITION, it.currentPosition)
        } ?: run {
            outState.putBoolean(KEY_PLAY_WHEN_READY, playWhenReady)
            outState.putLong(KEY_PLAYBACK_POSITION, playbackPosition)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        showActionBar()
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
        animateShadeToTransparent()
    }

    private fun showActionBar() {
        supportActionBar?.show()
        animateShadeToOriginal()
    }

    private fun animateShadeToTransparent() {
        if (binding.shade.alpha == 0f) {
            return
        }

        binding.shade.animate().apply {
            duration = 300
            alpha(0f)
        }
    }

    private fun animateShadeToOriginal() {
        if (binding.shade.alpha == 1f) {
            return
        }

        binding.shade.animate().apply {
            duration = 300
            alpha(1f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isSecureMode) {
            this.autoFinisher.stop()
        }
        player?.release()
        player = null
    }
}
