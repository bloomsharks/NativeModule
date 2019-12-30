package tgio.github.com.mediapickerlib

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap


object ObjectMapper {
    fun prepareResponse(pickMediaResponse: PickMediaResponse): WritableMap {
        return pickMediaResponse.toWritableMap()
    }

    fun constructMediaPickRequest(options: ReadableMap): PickMediaRequest {
        val pickMediaRequest: PickMediaRequest
        when(val mediaType = options.getString(KEY_MEDIATYPE)) {
            MEDIATYPE_PHOTO -> {
                val proportion = options.getString(KEY_PROPORTION)
                pickMediaRequest = Photo(
                    when(proportion) {
                        PHOTO_COVER -> Photo.Proportion.COVER
                        POST_TALL -> Photo.Proportion.POST_TALL
                        POST_WIDE -> Photo.Proportion.POST_WIDE
                        PHOTO_PROFILE  -> Photo.Proportion.PROFILE
                        else  -> throw RuntimeException("Unsoported proportion $proportion")
                    }
                )
            }
            MEDIATYPE_VIDEO -> {
                pickMediaRequest = Video()
            }
            MEDIATYPE_FILE -> {
                pickMediaRequest = Files()
            }
            else -> {
                throw RuntimeException("Unsoported mediaType $mediaType")
            }
        }
        if(options.hasKey(KEY_NEXT_BUTTON_STRING)) {
            pickMediaRequest.nextButtonString = options.getString(KEY_NEXT_BUTTON_STRING)
        }
        return pickMediaRequest
    }

    private const val KEY_MEDIATYPE = "mediaType"
    private const val KEY_PROPORTION = "proportion"
    private const val KEY_NEXT_BUTTON_STRING = "nextButtonString"

    private const val MEDIATYPE_PHOTO = "photo"
    private const val MEDIATYPE_VIDEO = "video"
    private const val MEDIATYPE_FILE = "file"

    private const val PHOTO_PROFILE = "profile"
    private const val PHOTO_COVER = "cover"
    private const val POST_TALL = "tall"
    private const val POST_WIDE = "wide"

}