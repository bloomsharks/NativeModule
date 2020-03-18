package tgio.github.com.mediapickerlib

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

object MetaDataUtils {

    class VideoMetaData(
        val thumbnailPath: String,
        val durationMillis: String,
        val width: String,
        val height: String
    ) {
        override fun toString(): String {
            return "VideoMetaData(thumbnailPath='$thumbnailPath', durationMillis='$durationMillis', width='$width', height='$height')"
        }
    }

    private fun getThumbnailAt(
        context: Context,
        retriever: MediaMetadataRetriever,
        at: Long,
        quality: Int
    ): String {
        val image = retriever.getFrameAtTime(at * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
        val fullPath = context.cacheDir.path + "/bloom_native_thumb_l"
        val dir = File(fullPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val fileName = "bloom_native_thumb_l-" + UUID.randomUUID().toString() + ".jpeg"
        val file = File(fullPath, fileName)
        file.createNewFile()
        try {
            val fOut: OutputStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, quality, fOut)
            fOut.flush()
            fOut.close()

        } catch (e: Exception) {
            return ""
        }

        return "file://$fullPath/$fileName"
    }

    fun getVideoMetaData(
        context: Context,
        filePath: Uri,
        thumbnailQuality: Int,
        atMs: Long = 0
    ): VideoMetaData {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, filePath)

        val thumbnail = getThumbnailAt(context, retriever, atMs, thumbnailQuality)

        try {
            val durationMillis =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val rotation = try {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    .toInt()
            } catch (e: Exception) {
                0
            }
            println("Rotation: $rotation")
            return if (rotation > 0 && rotation != 180) {
                VideoMetaData(thumbnail, durationMillis, height, width)
            } else {
                VideoMetaData(thumbnail, durationMillis, width, height)
            }
        } catch (ex: Exception) {
            throw ex
        }
    }

    fun getFileNameAndSize(context: Context, mUri: Uri): Pair<String, Long> {
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
            }
        }
        return Pair(originalFileName, size)
    }
}