package rajnishkmehta.sakshi.camera.capturer

import android.annotation.SuppressLint

import androidx.camera.video.internal.muxer.Muxer
import androidx.camera.video.internal.muxer.MuxerFactory
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn

/**
 * Factory class for creating instances of [Media3Mp4Muxer].
 *
 * Provides an initialization mechanism to supply custom MP4 muxers
 * for video capturing workflows.
 */
@OptIn(UnstableApi::class)
class Media3Mp4MuxerFactory : MuxerFactory {
    @SuppressLint("RestrictedApi")
    override fun create(outputFormat: Int): Muxer {
        return Media3Mp4Muxer()
    }
}
