package tgio.github.com.mediapickerlib

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

data class PickMediaResponse(
    val uri: String,
    val fileName: String,
    val type: String,
    val fileSize: String,
    val width: String? = null,
    val height: String? = null,
    val duration: String? = null,
    val thumbnail: String? = null
) {

    fun toBundle(): Bundle {
        val map = Bundle()

        map.putString("uri", uri)
        map.putString("fileName", fileName)
        map.putString("fileSize", fileSize)
        map.putString("type", type)

        //Optionals
        width?.let {
            map.putString("width", width)
        }

        height?.let {
            map.putString("height", height)
        }

        thumbnail?.let {
            map.putString("thumbnail", thumbnail)
        }
        duration?.let {
            map.putString("duration", duration)
        }

        return map
    }

    companion object {
        private fun handlePhoto(
            context: Context,
            intent: Intent,
            resolve: (Bundle) -> Unit,
            reject: (Throwable) -> Unit
        ) {
            val extras = intent.extras!!
            val uri = extras.getParcelable<Uri>("uri")!!
            val (fileName, size) = MetaDataUtils.getFileNameAndSize(context, uri)

            resolve.invoke(
                PickMediaResponse(
                    uri = uri.toString(),
                    fileName = fileName,
                    fileSize = size.toString(),
                    type = CommonUtils.getMimeType(context, uri),
                    height = intent.getStringExtra("imageHeight"),
                    width = intent.getStringExtra("imageWidth")
                ).toBundle()
            )
        }

        private fun handleVideo(
            context: Context,
            intent: Intent,
            resolve: (Bundle) -> Unit,
            reject: (Throwable) -> Unit
        ) {
            if(intent.data == null) {
                reject.invoke(Error.UNKNOWN.toThrowable())
            } else {
                var uri = intent.data!!.toString()
                //Viable if uri starts with Content or File
                val (_fileName, _fileSize) = MetaDataUtils.getFileNameAndSize(context, Uri.parse(uri))
                //Alternate option for data
                val fs = intent.getLongExtra(KEY_FILE_SIZE, _fileSize)
                val fileName = intent.getStringExtra(KEY_ORIGINAL_FILE_NAME) ?: _fileName

                var width = ""
                var height = ""
                var duration = ""
                var thumbnail = ""

                try {
                    val videoMetaData = MetaDataUtils.getVideoMetaData(
                        context,
                        Uri.parse(uri),
                        DEFAULT_COMPRESSION_QUALITY_THUMB
                    )
                    width = videoMetaData.width
                    height = videoMetaData.height
                    duration = videoMetaData.durationMillis
                    thumbnail = videoMetaData.thumbnailPath
                } catch (e: Exception) {
                    reject.invoke(e)
                }

                if(uri.startsWith("/data")) {
                    uri = "file://$uri"
                }

                resolve.invoke(
                    PickMediaResponse(
                        uri = uri,
                        fileName = fileName,
                        fileSize = fs.toString(),
                        type = CommonUtils.getMimeType(context, Uri.parse(uri)),
                        thumbnail = thumbnail,
                        height = height,
                        width = width,
                        duration = duration
                    ).toBundle()
                )
            }
        }

        private fun handleFile(
            context: Context,
            intent: Intent,
            resolve: (Bundle) -> Unit,
            reject: (Throwable) -> Unit
        ) {
            val uri = intent.data!!
            val (fileName, size) = MetaDataUtils.getFileNameAndSize(context, uri)

            resolve.invoke(
                PickMediaResponse(
                    uri = uri.toString(),
                    fileName = fileName,
                    fileSize = size.toString(),
                    type = CommonUtils.getMimeType(context, uri)
                ).toBundle()
            )
        }

        fun handle(
            context: Context,
            mediaRequest: PickMediaRequest,
            intent: Intent?,
            resolve: (Bundle) -> Unit,
            reject: (Throwable) -> Unit
        ) {
            if (intent == null) {
                resolve.invoke(CustomError(message = "Intent is null.").toBundle())
            } else when (mediaRequest.getRequestType()) {
                PICK_REQUEST_TYPE_PHOTO -> handlePhoto(context, intent, resolve, reject)
                PICK_REQUEST_TYPE_VIDEO -> handleVideo(context, intent, resolve, reject)
                PICK_REQUEST_TYPE_FILES -> handleFile(context, intent, resolve, reject)
                else -> reject.invoke(CustomError(message = "Unknown request type"))
            }
        }
    }
}