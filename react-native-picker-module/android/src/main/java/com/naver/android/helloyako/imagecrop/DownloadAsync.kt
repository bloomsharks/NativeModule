package com.naver.android.helloyako.imagecrop

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Size
import com.naver.android.helloyako.imagecrop.util.BitmapLoadUtils
import tgio.github.com.mediapickerlib.CommonUtils
import tgio.github.com.mediapickerlib.Photo

class DownloadAsync(
    val context: Context,
    val photo: Photo,
    val callback: (bitmap: Bitmap, ratioX: Int, ratioY: Int) -> Unit
) : AsyncTask<Uri, Void, Triple<Bitmap, Int, Int>>() {

    private var mProgress: ProgressDialog? = null
    private var mUri: Uri? = null

    override fun onPreExecute() {
        super.onPreExecute()

        mProgress = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        mProgress?.isIndeterminate = true
        mProgress?.setCancelable(false)
        mProgress?.setMessage("Loading image...")
        mProgress?.show()
    }

    override fun doInBackground(vararg params: Uri): Triple<Bitmap, Int, Int>? {
        mUri = params[0]


        var bitmap: Bitmap? = null
        mUri?.let {
            bitmap = BitmapLoadUtils.decode(it.toString(), 2000, 2000, true)
        }

        val size = if(photo.ratioX == 0 || photo.ratioY == 0) {
            if(bitmap!!.width > bitmap!!.height) {
                Size(4, 3)
            } else {
                Size(3, 4)
            }
        } else {
            Size(photo.ratioX, photo.ratioY)
        }

        return Triple(bitmap!!, size.width, size.height)
    }

    override fun onPostExecute(result: Triple<Bitmap, Int, Int>?) {
        super.onPostExecute(result)

        mProgress?.dismiss()
        result?.let {
            mUri?.let { uri ->
                callback.invoke(result.first, result.second, result.third)
            }
        }
    }

    override fun onCancelled() {
        super.onCancelled()
    }
}