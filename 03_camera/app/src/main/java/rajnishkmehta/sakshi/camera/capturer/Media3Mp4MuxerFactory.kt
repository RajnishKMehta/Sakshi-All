package rajnishkmehta.sakshi.camera.capturer

import android.annotation.SuppressLint
import rajnishkmehta.sakshi.camera.debug.DebugLogger as Log
import androidx.camera.video.internal.muxer.Muxer
import androidx.camera.video.internal.muxer.MuxerFactory
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn

@OptIn(UnstableApi::class)
class Media3Mp4MuxerFactory : MuxerFactory {
    @SuppressLint("RestrictedApi")
    override fun create(outputFormat: Int): Muxer {
        Log.d("Media3Mp4MuxerFactory", "create Muxer, outputFormat: $outputFormat")
        return Media3Mp4Muxer()
    }
}
