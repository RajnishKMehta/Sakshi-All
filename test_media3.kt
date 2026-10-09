import androidx.media3.muxer.FragmentedMp4Muxer
import androidx.media3.muxer.Muxer
import androidx.media3.common.Format

fun test(muxer: FragmentedMp4Muxer) {
    muxer.addMetadataEntry(androidx.media3.container.Mp4OrientationData(90))
}
