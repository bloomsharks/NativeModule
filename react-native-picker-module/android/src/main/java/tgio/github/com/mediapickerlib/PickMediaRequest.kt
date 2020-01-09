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
    val maxFileSizeBytes: Int,
    val compressionQuality: Int,
    val maxBitmapSize: Int,
    val maxScaleMultiplier: Float
) : PickMediaRequest() {
    sealed class Proportion(open val x: Float, open val y: Float) {
        object PROFILE : Proportion(1F, 1F)
        object COVER : Proportion(343F, 136F)
        object POST : Proportion(0F, 0F)
        data class CUSTOM(override val x: Float, override val y: Float) : Proportion(x, y)
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

