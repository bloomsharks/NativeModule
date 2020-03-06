package tgio.github.com.mediapickerlib

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
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
    ) : Parcelable {
        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<VideoMetaData> =
                object : Parcelable.Creator<VideoMetaData> {
                    override fun createFromParcel(source: Parcel): VideoMetaData =
                        VideoMetaData(source)

                    override fun newArray(size: Int): Array<VideoMetaData?> = arrayOfNulls(size)
                }
        }

        constructor(source: Parcel): this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString()
        )

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
            writeString(thumbnailPath)
            writeString(durationMillis)
            writeString(width)
            writeString(height)
        }

        override fun toString(): String {
            return "VideoMetaData(thumbnailPath='$thumbnailPath', durationMillis='$durationMillis', width='$width', height='$height')"
        }
    }

    fun getVideoMetaData(
        context: Context,
        filePath: Uri,
        thumbnailQuality: Int
    ): VideoMetaData {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, filePath)
        val image = retriever.getFrameAtTime(
            0,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        )

        val fullPath = context.cacheDir.path + "/bloom_native_thumb_l"
        try {
            val dir = File(fullPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = "bloom_native_thumb_l-" + UUID.randomUUID().toString() + ".jpeg"
            val file = File(fullPath, fileName)
            file.createNewFile()
            val fOut: OutputStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, thumbnailQuality, fOut)
            fOut.flush()
            fOut.close()
            val thumbnail = "file://$fullPath/$fileName"
            val durationMillis =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val rotation = try {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION).toInt()
            } catch (e: Exception) {
                0
            }
            println("videoMetaData rotation:$rotation")
            if(rotation > 0 && rotation != 180) {
                return VideoMetaData(thumbnail, durationMillis, height, width)
            } else {
                return VideoMetaData(thumbnail, durationMillis, width, height)
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