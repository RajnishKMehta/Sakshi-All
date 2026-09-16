package rajnishkmehta.sakshi.camera.capturer

import android.annotation.SuppressLint

import androidx.camera.video.internal.muxer.Muxer
import androidx.camera.video.internal.muxer.MuxerFactory

class FragmentedMedia3MuxerFactory : MuxerFactory {
    @SuppressLint("RestrictedApi")
    override fun create(outputFormat: Int): Muxer {
        return FragmentedMedia3Muxer()
    }
}
