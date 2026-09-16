package rajnishkmehta.sakshi.camera.capturer

import android.annotation.SuppressLint

import androidx.camera.video.internal.muxer.Muxer
import androidx.camera.video.internal.muxer.MuxerFactory

/**
 * Factory class for creating instances of [FragmentedMedia3Muxer].
 *
 * Provides an initialization mechanism to supply custom fragmented MP4 muxers
 * for video capturing workflows.
 */
@androidx.media3.common.util.UnstableApi
class FragmentedMedia3MuxerFactory : MuxerFactory {
    @SuppressLint("RestrictedApi")
    override fun create(outputFormat: Int): Muxer {
        return FragmentedMedia3Muxer()
    }
}
