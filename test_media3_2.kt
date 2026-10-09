import androidx.media3.muxer.FragmentedMp4Muxer
import androidx.media3.common.Format

fun test() {
    val muxer = FragmentedMp4Muxer.Builder(java.io.FileOutputStream("test").channel).build()
}
