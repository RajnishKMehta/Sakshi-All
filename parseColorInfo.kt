import android.media.MediaFormat
import androidx.media3.common.ColorInfo

fun parseColorInfo(format: MediaFormat): ColorInfo? {
    val colorStandard = if (format.containsKey(MediaFormat.KEY_COLOR_STANDARD)) format.getInteger(MediaFormat.KEY_COLOR_STANDARD) else Format.NO_VALUE
    val colorRange = if (format.containsKey(MediaFormat.KEY_COLOR_RANGE)) format.getInteger(MediaFormat.KEY_COLOR_RANGE) else Format.NO_VALUE
    val colorTransfer = if (format.containsKey(MediaFormat.KEY_COLOR_TRANSFER)) format.getInteger(MediaFormat.KEY_COLOR_TRANSFER) else Format.NO_VALUE
    val hdrStaticInfo = if (format.containsKey("hdr-static-info")) format.getByteBuffer("hdr-static-info")?.array() else null
    return if (colorStandard != Format.NO_VALUE || colorRange != Format.NO_VALUE || colorTransfer != Format.NO_VALUE || hdrStaticInfo != null) {
        ColorInfo.Builder()
            .setColorSpace(colorStandard)
            .setColorRange(colorRange)
            .setColorTransfer(colorTransfer)
            .setHdrStaticInfo(hdrStaticInfo)
            .build()
    } else null
}
