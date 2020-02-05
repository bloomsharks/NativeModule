package tgio.github.com.mediapickerlib

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore

sealed class PickMediaRequest {
    abstract fun getRequestType(): Int
    abstract fun getIntent(): Intent
    open var nextButtonString: String? = null
}

data class Photo(
    val ratioX: Int,
    val ratioY: Int,
    val maxFileSizeBytes: Int,
    val compressionQuality: Int,
    val maxBitmapSize: Int,
    val maxScaleMultiplier: Float,
    val skipCrop: Boolean = false
) : PickMediaRequest() {
    override fun getRequestType(): Int = PICK_REQUEST_TYPE_PHOTO
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

    companion object {
        fun fromBundle(bundle: Bundle): Photo {
            return Photo(
                ratioX = bundle.getInt("x", DEFAULT_RATIO),
                ratioY = bundle.getInt("y", DEFAULT_RATIO),
                maxFileSizeBytes = bundle.getInt("maxFileSizeBytes", DEFAULT_MAX_FILE_SIZE_BYTES),
                compressionQuality = bundle.getInt("compressionQuality", DEFAULT_COMPRESSION_QUALITY),
                maxBitmapSize = bundle.getInt("maxBitmapSize", DEFAULT_MAX_BITMAP_SIZE),
                maxScaleMultiplier = bundle.getFloat("maxScaleMultiplier", DEFAULT_MAX_ZOOM),
                skipCrop = bundle.getBoolean("skipCrop", false)
            )
        }
    }

    fun toBundle(): Bundle {
        return Bundle().also {
            it.putInt("x", ratioX)
            it.putInt("y", ratioY)
            it.putInt("maxFileSizeBytes", maxFileSizeBytes)
            it.putInt("compressionQuality", compressionQuality)
            it.putInt("maxBitmapSize", maxBitmapSize)
            it.putFloat("maxScaleMultiplier", maxScaleMultiplier)
            it.putBoolean("skipCrop", skipCrop)
        }
    }

    override fun toString(): String {
        return "Photo(ratioX=$ratioX, ratioY=$ratioY, maxFileSizeBytes=$maxFileSizeBytes, compressionQuality=$compressionQuality, maxBitmapSize=$maxBitmapSize, maxScaleMultiplier=$maxScaleMultiplier)"
    }
}

class Video(
    val trim: Boolean,
    val compress: Boolean,
    val encode: Boolean,
    val minDurationSeconds: Int,
    val maxDurationSeconds: Int,
    val staticText: String,
    val maxDisplayedThumbs: Int = DEFAULT_MAX_DISPLAYED_THUMBS
) : PickMediaRequest() {

    fun toBundle(): Bundle {
        return Bundle().also {
            it.putBoolean(KEY_DO_TRIM, trim)
            it.putBoolean(KEY_DO_ENCODE, encode)
            it.putBoolean(KEY_COMPRESS_AFTER_TRIM, compress)
            it.putString(KEY_STATIC_TEXT, staticText)
            it.putInt(KEY_MIN_SECONDS, minDurationSeconds)
            it.putInt(KEY_MAX_SECONDS, maxDurationSeconds)
            it.putInt(KEY_MAX_DISPLAYED_THUMBS, maxDisplayedThumbs)
        }
    }

    override fun getRequestType(): Int = PICK_REQUEST_TYPE_VIDEO

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
    override fun getRequestType(): Int = PICK_REQUEST_TYPE_FILES

    override fun getIntent(): Intent {
        return Intent.createChooser(
            Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*"),
            "Pick File"
        )
    }
}

