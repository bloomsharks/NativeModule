package tgio.github.com.mediapickerlib

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.IntRange
import androidx.core.net.toFile
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

object MetaDataUtils {
    fun getVideoMetaData(
        context: Context,
        filePath: Uri,
        @IntRange(from = 10, to = 100) thumbnailQuality: Int,
        originalName: String,
        fileSizeBytes: Long
    ): PickVideoResponse.VideoMetaData {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, filePath)
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
            fileSizeBytes = fileSizeBytes.toString()
        )
    }

    fun getFileNameAndSize(context: Context, mUri: Uri): Pair<Long, String> {
        var size = 0L
        var originalFileName = ""
        when (mUri.scheme) {
            "content" -> {
                context.contentResolver.query(mUri, null, null, null, null).use { returnCursor ->
                    if (returnCursor != null && returnCursor.moveToFirst()) {
                        val nameIndex: Int =
                            returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
                        size = returnCursor.getLong(sizeIndex)
                        originalFileName = returnCursor.getString(nameIndex)
                    }
                }
            }
            "file" -> {
                val ff = mUri.toFile()
                size = ff.length()
                originalFileName = CommonUtils.getFileName(ff.path)
            }
            else -> {
                println("getFileNameAndSize($mUri) >>> unknown scheme ${mUri.scheme}")
            }
        }
        return Pair(size, originalFileName)
    }
}