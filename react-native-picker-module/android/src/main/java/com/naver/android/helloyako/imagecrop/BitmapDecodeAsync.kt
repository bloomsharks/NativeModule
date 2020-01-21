package com.naver.android.helloyako.imagecrop

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import tgio.github.com.mediapickerlib.Photo
import java.io.File
import java.io.FileOutputStream

class BitmapDecodeAsync(
    val context: Context,
    private val bitmap: Bitmap,
    private val photo: Photo,
    private val destination: File,
    private val callback: (File) -> Unit
) : AsyncTask<Uri, Void, File>() {

    private var mProgress: ProgressDialog? = null
    private var mUri: Uri? = null

    override fun onPreExecute() {
        super.onPreExecute()

        mProgress = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        mProgress?.isIndeterminate = true
        mProgress?.setMessage("Compressing image...")
        mProgress?.setCancelable(false)
        mProgress?.show()
    }

    override fun doInBackground(vararg params: Uri): File {
        println("XKA bitmapConvertToFile $photo")
        FileOutputStream(destination).use { fileOutputStream ->

            if (photo.maxFileSizeBytes == 0) {
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    photo.compressionQuality,
                    fileOutputStream
                )
//                bitmap.recycle()
            } else {
                var currSize = 0L
                var currQuality = photo.compressionQuality
                do {
                    context.contentResolver.openOutputStream(Uri.fromFile(destination))
                        .use { outputStream ->
                            val compressed = bitmap.compress(
                                Bitmap.CompressFormat.JPEG,
                                currQuality,
                                outputStream
                            )
                            currSize = destination.length()
                            // limit quality by 5 percent every time
                            println("XCC compressed $compressed currSize $currSize maxFileSizeBytes ${photo.maxFileSizeBytes} currQuality $currQuality")
                            currQuality -= 5
                            if (currSize >= photo.maxFileSizeBytes) {
                                destination.delete()
                            }
                        }
                } while (currSize >= photo.maxFileSizeBytes)
            }
            scanFile(context, destination, callback)
            return destination
        }
    }

    override fun onPostExecute(result: File) {
        super.onPostExecute(result)

        mProgress?.dismiss()
        result.let {
            mUri?.let { uri ->
                callback.invoke(result)
            }
        }
    }

    private fun scanFile(
        context: Context,
        file: File,
        callback: (File) -> Unit
    ) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            null,
            object : MediaScannerConnection.MediaScannerConnectionClient {
                override fun onMediaScannerConnected() {
                    println("onMediaScannerConnected")
                }

                override fun onScanCompleted(path: String, uri: Uri?) {
                    println("onScanCompleted path = [${path}], uri = [${uri}]")
                    callback.invoke(file)
                }
            }
        )
    }
}