package tgio.github.com.mediapickerlib

import com.facebook.react.bridge.ReadableMap

object ObjectMapper {
    fun constructMediaPickRequest(options: ReadableMap): PickMediaRequest {
        val pickMediaRequest: PickMediaRequest
        if(options.hasKey(KEY_MEDIATYPE).not()) {
            throw RuntimeException("Missing key 'mediaType'")
        }
        when (val mediaType = options.getString(KEY_MEDIATYPE)) {
            MEDIATYPE_PHOTO -> {
                pickMediaRequest = Photo(
                    ratioX = when {
                        options.hasKey(KEY_PHOTO_RATIO_X) -> {
                            options.getInt(KEY_PHOTO_RATIO_X)
                        }
                        options.hasKey(KEY_PHOTO_PROPORTION) -> {
                            when(options.getString(KEY_PHOTO_PROPORTION)) {
                                KEY_PHOTO_PROFILE -> DEFAULT_RATIO
                                KEY_PHOTO_COVER -> DEFAULT_RATIO_COVER_X
                                KEY_PHOTO_POST -> 0
                                else -> DEFAULT_RATIO
                            }
                        }
                        else -> DEFAULT_RATIO
                    },
                    ratioY = when {
                        options.hasKey(KEY_PHOTO_RATIO_Y) -> {
                            options.getInt(KEY_PHOTO_RATIO_Y)
                        }
                        options.hasKey(KEY_PHOTO_PROPORTION) -> {
                            when(options.getString(KEY_PHOTO_PROPORTION)) {
                                KEY_PHOTO_PROFILE -> DEFAULT_RATIO
                                KEY_PHOTO_COVER -> DEFAULT_RATIO_COVER_Y
                                KEY_PHOTO_POST -> 0
                                else -> DEFAULT_RATIO
                            }
                        }
                        else -> DEFAULT_RATIO
                    },
                    maxFileSizeBytes = if (options.hasKey(KEY_PHOTO_MAX_FILE_SIZE_BYTES)) {
                        options.getInt(KEY_PHOTO_MAX_FILE_SIZE_BYTES)
                    } else DEFAULT_MAX_FILE_SIZE_BYTES,
                    compressionQuality = if (options.hasKey(KEY_PHOTO_COMPRESSION_QUALITY)) {
                        options.getInt(KEY_PHOTO_COMPRESSION_QUALITY)
                    } else DEFAULT_COMPRESSION_QUALITY,
                    maxScaleMultiplier = if (options.hasKey(KEY_PHOTO_MAX_SCALE_MULTIPLIER)) {
                        options.getString(KEY_PHOTO_MAX_SCALE_MULTIPLIER).toFloat()
                    } else DEFAULT_MAX_ZOOM,
                    maxBitmapSize = if (options.hasKey(KEY_PHOTO_MAX_BITMAP_SIZE)) {
                        options.getInt(KEY_PHOTO_MAX_BITMAP_SIZE)
                    } else DEFAULT_MAX_BITMAP_SIZE,
                    skipCrop = if(options.hasKey(KEY_PHOTO_SKIP_CROP)) {
                        options.getBoolean(KEY_PHOTO_SKIP_CROP)
                    } else DEFAULT_PHOTO_SKIP_CROP
                )
            }
            MEDIATYPE_VIDEO -> {
                pickMediaRequest = Video(
                    trim = if(options.hasKey(KEY_DO_TRIM)) {
                        options.getBoolean(KEY_DO_TRIM)
                    } else DEFAULT_KEY_DO_TRIM,
                    compress = if(options.hasKey(KEY_COMPRESS_AFTER_TRIM)) {
                        options.getBoolean(KEY_COMPRESS_AFTER_TRIM)
                    } else DEFAULT_COMPRESS_AFTER_TRIM,
                    minDurationSeconds = if(options.hasKey(KEY_MIN_SECONDS)) {
                        options.getInt(KEY_MIN_SECONDS)
                    } else DEFAULT_MIN_SECONDS,
                    maxDurationSeconds = if(options.hasKey(KEY_MAX_SECONDS)) {
                        options.getInt(KEY_MAX_SECONDS)
                    } else DEFAULT_MAX_SECONDS,
                    maxDisplayedThumbs = if(options.hasKey(KEY_MAX_DISPLAYED_THUMBS)) {
                        options.getInt(KEY_MAX_DISPLAYED_THUMBS)
                    } else DEFAULT_MAX_DISPLAYED_THUMBS,
                    encode = if(options.hasKey(KEY_DO_ENCODE)) {
                        options.getBoolean(KEY_DO_ENCODE)
                    } else DEFAULT_DO_ENCODE
                )
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
