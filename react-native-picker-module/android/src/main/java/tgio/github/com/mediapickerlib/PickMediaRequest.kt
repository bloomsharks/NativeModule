package tgio.github.com.mediapickerlib

import android.provider.MediaStore

sealed class PickMediaRequest {
    companion object {
        const val REQUEST_PHOTO = 1337
        const val REQUEST_VIDEO = 1338
        const val REQUEST_FILE = 1339
    }

    open var nextButtonString: String? = null
}

data class Photo(
    val proportion: Proportion,
    val maxFileSizeBytes: Int = 8 * 1024 * 1024,
    val maxResultWidth: Int = 2048,
    val maxResultHeight: Int = 2048
) : PickMediaRequest() {
    enum class Proportion(val x: Float, val y: Float) {
        PROFILE(1F, 1F),
        COVER(343F, 136F),
        POST_TALL(3F, 4F),
        POST_WIDE(4F, 3F),
    }

    companion object {
        val INTENT_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val INTENT_TYPE = "image/*"
        val MIME_TYPES = arrayOf("image/jpeg", "image/png", "image/gif")
    }
}

class Video : PickMediaRequest() {
    companion object {
        val INTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val INTENT_TYPE = "video/*"
        val MIME_TYPES = arrayOf("video/mp4")
    }
}

class Files : PickMediaRequest() {
    companion object {
        val INTENT_TYPE = "*/*"
    }
}

