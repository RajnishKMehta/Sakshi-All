package rajnishkmehta.sakshi.camera.capturer

import androidx.camera.video.internal.muxer.Muxer
import androidx.camera.video.internal.muxer.MuxerFactory

class FragmentedMedia3MuxerFactory : MuxerFactory {
    override fun create(outputFormat: Int): Muxer {
        return FragmentedMedia3Muxer()
    }
}
