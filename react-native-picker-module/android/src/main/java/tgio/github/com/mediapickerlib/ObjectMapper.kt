package tgio.github.com.mediapickerlib

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap

object ObjectMapper {
    fun prepareResponse(pickMediaResponse: PickMediaResponse): WritableMap {
        return pickMediaResponse.toWritableMap()
    }

    fun constructMediaPickRequest(options: ReadableMap): PickMediaRequest {
        val pickMediaRequest: PickMediaRequest
        if(options.hasKey(KEY_MEDIATYPE).not()) {
            throw RuntimeException("Missing key 'mediaType'")
        }
        when (val mediaType = options.getString(KEY_MEDIATYPE)) {
            MEDIATYPE_PHOTO -> {
                val proportion = options.getString(KEY_PHOTO_PROPORTION)
                val x = if (options.hasKey(KEY_PHOTO_X)) {
                    options.getString(KEY_PHOTO_X).toFloat()
                } else 0F
                val y = if (options.hasKey(KEY_PHOTO_Y)) {
                    options.getString(KEY_PHOTO_Y).toFloat()
                } else 0F
                pickMediaRequest = Photo(
                    when (proportion) {
                        PHOTO_COVER -> Photo.Proportion.COVER
                        PHOTO_PROFILE -> Photo.Proportion.PROFILE
                        PHOTO_POST -> Photo.Proportion.POST
                        PHOTO_CUSTOM -> Photo.Proportion.CUSTOM(x, y)
                        else -> throw RuntimeException("`proportion` [$proportion] not supported")
                    },
                    maxFileSizeBytes = if (options.hasKey(KEY_PHOTO_MAX_FILE_SIZE_BYTES)) {
                        options.getInt(KEY_PHOTO_MAX_FILE_SIZE_BYTES)
                    } else 0,
                    compressionQuality = if (options.hasKey(KEY_PHOTO_COMPRESSION_QUALITY)) {
                        options.getInt(KEY_PHOTO_COMPRESSION_QUALITY)
                    } else 60,
                    maxScaleMultiplier = if (options.hasKey(KEY_PHOTO_MAX_SCALE_MULTIPLIER)) {
                        options.getString(KEY_PHOTO_MAX_SCALE_MULTIPLIER).toFloat()
                    } else 10F,
                    maxBitmapSize = if (options.hasKey(KEY_PHOTO_MAX_BITMAP_SIZE)) {
                        options.getInt(KEY_PHOTO_MAX_BITMAP_SIZE)
                    } else 10000
                )
            }
            MEDIATYPE_VIDEO -> {
                pickMediaRequest = Video()
            }
            MEDIATYPE_FILE -> {
                pickMediaRequest = Files()
            }
            else -> {
                throw RuntimeException("`mediaType` [$mediaType] not supported")
            }
        }

        if (options.hasKey(KEY_NEXT_BUTTON_STRING)) {
            val string = options.getString(KEY_NEXT_BUTTON_STRING)
            if(string.isNotEmpty()) {
                pickMediaRequest.nextButtonString = string
            } else {
                throw RuntimeException("'nextButtonString' is provided but it is empty.")
            }
        }
        return pickMediaRequest
    }
}
