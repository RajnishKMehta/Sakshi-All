package rajnishkmehta.sakshi.portal.ui.gallery

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.ui.PlayerView

import androidx.compose.runtime.produceState
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.SakshiClientConfig
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import rajnishkmehta.sakshi.portal.data.SettingsRepository
import kotlinx.coroutines.flow.first

import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn

@OptIn(UnstableApi::class)
@Composable
fun MediaViewerScreen(
    uri: String, // Actually fileId
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }

    val realUriState = produceState<String?>(initialValue = null, uri) {
        val vaultPackage = settingsRepository.vaultPackageFlow.first() ?: "rajnishkmehta.sakshi.vault"
        val config = SakshiClientConfig(vaultPackageName = vaultPackage, connectionTimeoutMs = 5000L)
        val client = SakshiClient.create(context, config)
        val response = client.getMedia(uri)
        if (response.isSuccess) {
            value = response.getOrNull()?.contentUri
        } else {
            value = "error"
        }
        client.disconnect()
    }

    val realUri = realUriState.value
    if (realUri == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (realUri == "error") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error loading media", color = Color.White)
        }
    }

    val parsedUri = Uri.parse(realUri)

    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }

    val exoPlayer = remember(parsedUri) {
        val extractorsFactory = DefaultExtractorsFactory()
            .setFragmentedMp4ExtractorFlags(
                FragmentedMp4Extractor.FLAG_ENABLE_EMSG_TRACK or
                FragmentedMp4Extractor.FLAG_READ_MFRA_FOR_SEEK_MAP
            )
        val mediaSourceFactory = DefaultMediaSourceFactory(context, extractorsFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(parsedUri))
                prepare()
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingState: Boolean) {
                        isPlaying = isPlayingState
                    }
                })
            }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            // Auto hide controls logic could go here if using a custom Compose player.
            // Since we are using AndroidView with PlayerView, it handles its own auto-hide.
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    }
                )
            }
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            update = { playerView ->
                if (showControls) {
                    playerView.showController()
                } else {
                    playerView.hideController()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
