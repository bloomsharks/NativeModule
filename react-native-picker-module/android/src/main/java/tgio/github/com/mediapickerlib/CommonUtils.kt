package tgio.github.com.mediapickerlib

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Size
import android.webkit.MimeTypeMap
import java.io.File


object CommonUtils {

    fun getMimeType(context: Context, uri: Uri): String {
        val cR: ContentResolver = context.contentResolver

        var type = cR.getType(uri)

        if(type == null) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.path)
            if (extension != null) {
                val mime = MimeTypeMap.getSingleton()
                type = mime.getMimeTypeFromExtension(extension)
            }
        }
        return type ?: "Unknown"
    }

    fun getImageDimensions(path: String): Size {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(path).absolutePath, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        val size = Size(imageWidth, imageHeight)
        return size
    }

    fun getFileName(path: String): String {
        return path.substring(path.lastIndexOf("/") + 1)
    }
}