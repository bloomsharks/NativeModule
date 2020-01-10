package tgio.github.com.mediapickerlib

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

sealed class PickMediaResponse(
    open val uri: String
) {
    abstract fun toWritableMap(): WritableMap
}

data class PickPhotoResponse(
    val mediaRequest: PickMediaRequest,
    override val uri: String,
    val metadata: PhotoMetaData
) : PickMediaResponse(uri) {
    override fun toWritableMap(): WritableMap {
        val map = Arguments.createMap()
        map.putString("uri", uri)
        map.putString("fileName", metadata.fileName)
        map.putInt("width", metadata.width)
        map.putInt("height", metadata.height)
        map.putInt("fileSize", metadata.fileSizeBytes.toInt())
        return map
    }
    data class PhotoMetaData(
        val width: Int,
        val height: Int,
        val fileName: String,
        val fileSizeBytes: String
    )
}

data class PickVideoResponse(
    val mediaRequest: PickMediaRequest,
    override val uri: String,
    val metadata: VideoMetaData
) : PickMediaResponse(uri) {
    override fun toWritableMap(): WritableMap {
        val map = Arguments.createMap()
        map.putString("uri", uri)
        map.putString("fileName", metadata.fileName)
        map.putInt("width", metadata.width?.toInt() ?: 0)
        map.putInt("height", metadata.height?.toInt() ?: 0)
        map.putInt("fileSize", metadata.fileSizeBytes?.toInt() ?: 0)
        map.putInt("durationMillis", metadata.durationMillis?.toInt() ?: 0)
        map.putString("thumbnail", metadata.thumbnail)
        map.putString("contentUri", metadata.contentUri)
        return map
    }
    data class VideoMetaData(
        val height: String? = null,
        val width: String? = null,
        val fileName: String? = null,
        val fileSizeBytes: String? = null,
        val durationMillis: String? = null,
        val thumbnail: String? = null,
        val contentUri: String? = null
    )
}

data class PickFileResponse(
    val mediaRequest: PickMediaRequest,
    override val uri: String,
    val metadata: FileMetaData
) : PickMediaResponse(uri) {
    override fun toWritableMap(): WritableMap {
        val map = Arguments.createMap()
        map.putString("uri", uri)
        map.putString("fileName", metadata.fileName)
        map.putInt("fileSize", metadata.fileSizeBytes?.toInt() ?: 0)
        return map
    }
    data class FileMetaData(
        val fileName: String? = null,
        val fileSizeBytes: String? = null
    )
}