package tgio.github.com.mediapickerlib

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap

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

    fun toBundle(): WritableMap {
        val map = Arguments.createMap()

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
            promise: Promise
        ) {
            val extras = intent.extras!!
            val uri = extras.getParcelable<Uri>("uri")!!
            val (fileName, size) = MetaDataUtils.getFileNameAndSize(context, uri)

            promise.resolve(
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
            promise: Promise
        ) {
            val uri = intent.data!!
            val (_, size) = MetaDataUtils.getFileNameAndSize(context, uri)

            val fileName = intent.getStringExtra(KEY_ORIGINAL_FILE_NAME)
            var width: String? = null
            var height: String? = null
            var duration: String? = null
            var thumbnail: String? = null

            try {
                val videoMetaData = MetaDataUtils.getVideoMetaData(
                    context,
                    uri,
                    DEFAULT_COMPRESSION_QUALITY_THUMB
                )
                width = videoMetaData.width
                height = videoMetaData.height
                duration = videoMetaData.durationMillis
                thumbnail = videoMetaData.thumbnailPath
            } catch (e: Exception) {
                promise.reject(e)
            }

            promise.resolve(
                PickMediaResponse(
                    uri = uri.toString(),
                    fileName = fileName,
                    fileSize = size.toString(),
                    type = CommonUtils.getMimeType(context, uri),
                    thumbnail = thumbnail,
                    height = height,
                    width = width,
                    duration = duration
                ).toBundle()
            )
        }

        private fun handleFile(
            context: Context,
            intent: Intent,
            promise: Promise
        ) {
            val uri = intent.data!!
            val (fileName, size) = MetaDataUtils.getFileNameAndSize(context, uri)

            promise.resolve(
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
            promise: Promise
        ) {
            if (intent == null) {
                promise.resolve(CustomError(message = "Intent is null.").toBundle())
            } else when (mediaRequest.getRequestType()) {
                PICK_REQUEST_TYPE_PHOTO -> handlePhoto(context, intent, promise)
                PICK_REQUEST_TYPE_VIDEO -> handleVideo(context, intent, promise)
                PICK_REQUEST_TYPE_FILES -> handleFile(context, intent, promise)
                else -> promise.resolve(CustomError(message = "Unknown request type").toBundle())
            }
        }
    }
}