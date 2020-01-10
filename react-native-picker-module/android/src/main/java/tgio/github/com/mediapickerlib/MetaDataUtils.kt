package tgio.github.com.mediapickerlib

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.IntRange
import tgio.github.com.mediapickerlib.PickVideoResponse
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

object MetaDataUtils {
    fun getVideoMetaData(
        context: Context, filePath: String,
        @IntRange(from = 10, to = 100) thumbnailQuality: Int,
        originalName: String,
        contentUri: Uri?
    ): PickVideoResponse.VideoMetaData {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        val image = retriever.getFrameAtTime(
            1000000,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        )

        val fullPath = context.cacheDir.path + "/thumb"
        var thumbnail: String? = null
        try {
            val dir = File(fullPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = "thumb-" + UUID.randomUUID().toString() + ".jpeg"
            val file = File(fullPath, fileName)
            file.createNewFile()
            val fOut: OutputStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, thumbnailQuality, fOut)
            fOut.flush()
            fOut.close()
            thumbnail = "file://$fullPath/$fileName"
        } catch (ignored: Exception) { }
        return PickVideoResponse.VideoMetaData(
            thumbnail = thumbnail,
            durationMillis = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
            height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT),
            width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
            fileName = originalName,
            fileSizeBytes = File(filePath).length().toString(),
            contentUri = contentUri.toString()
        )
    }
}