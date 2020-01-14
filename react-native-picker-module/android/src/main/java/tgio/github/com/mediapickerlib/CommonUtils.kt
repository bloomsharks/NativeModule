package tgio.github.com.mediapickerlib

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Size
import android.webkit.MimeTypeMap
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import java.io.File

object CommonUtils {

    fun getRealPath(context: Context, uri: Uri, callback: (String?) -> Unit) {
        PickiT(context, object : PickiTCallbacks {
            override fun PickiTonCompleteListener(
                path: String?,
                wasDriveFile: Boolean,
                wasUnknownProvider: Boolean,
                wasSuccessful: Boolean,
                originalFileName: String,
                originalFileSize: Int,
                Reason: String?
            ) = callback.invoke(path)
        }).getPath(uri)
    }

    fun getImageDimensions(path: String, callback: (Size) -> Unit) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(path).absolutePath, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        val size = Size(imageWidth, imageHeight)
        callback.invoke(size)
    }

    fun getImageDimensions(context: Context, uri: Uri, callback: (Size) -> Unit) {
        getRealPath(context, uri) { path ->
            path?.let {
                getImageDimensions(path) { size ->
                    callback.invoke(size)
                }
            }
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        val cR: ContentResolver = context.contentResolver
        return cR.getType(uri)
    }

    fun getFileName(path: String): String {
        return path.substring(path.lastIndexOf("/") + 1)
    }
}