package tgio.github.com.mediapickerlib

import android.content.Intent
import android.provider.MediaStore

sealed class PickMediaRequest {
    abstract fun getIntent(): Intent
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

    override fun getIntent(): Intent {
        return Intent.createChooser(
            Intent(
                Intent.ACTION_OPEN_DOCUMENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).setType("image/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf("image/jpeg", "image/png", "image/gif")
                ),
            "Pick Photo"
        )
    }
}

class Video : PickMediaRequest() {
    override fun getIntent(): Intent {
        return Intent.createChooser(
            Intent(
                Intent.ACTION_OPEN_DOCUMENT,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ).setType("video/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/mp4")),
            "Pick Video"
        )
    }
}

class Files : PickMediaRequest() {
    override fun getIntent(): Intent {
        return Intent.createChooser(
            Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*"),
            "Pick File"
        )
    }
}

