import androidx.media3.common.ColorInfo
import androidx.media3.common.Format

fun test(builder: Format.Builder) {
    builder.setColorInfo(ColorInfo.Builder().build())
}
