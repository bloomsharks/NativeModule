package tgio.github.com.mediapickerlib

const val KEY_MEDIATYPE = "mediaType"
const val KEY_NEXT_BUTTON_STRING = "nextButtonString"

const val KEY_PHOTO_PROPORTION = "proportion"
const val KEY_PHOTO_RATIO_X = "ratioX"
const val KEY_PHOTO_RATIO_Y = "ratioY"
const val KEY_PHOTO_MAX_FILE_SIZE_BYTES = "maxFileSizeBytes"
const val KEY_PHOTO_COMPRESSION_QUALITY = "compressionQuality"
const val KEY_PHOTO_MAX_SCALE_MULTIPLIER = "maxScaleMultiplier"
const val KEY_PHOTO_MAX_BITMAP_SIZE = "maxBitmapSize"
const val KEY_PHOTO_SKIP_CROP = "skipCrop"

const val KEY_PHOTO_PROFILE = "profile"
const val KEY_PHOTO_COVER = "cover"
const val KEY_PHOTO_POST = "post"

const val MEDIATYPE_PHOTO = "photo"
const val MEDIATYPE_VIDEO = "video"
const val MEDIATYPE_FILE = "file"


const val REQUEST_CODE_CROP_IMAGE = 1336
const val REQUEST_PICK_PHOTO = 1337
const val REQUEST_PICK_VIDEO = 1338
const val REQUEST_TRIM_VIDEO = 1340
const val REQUEST_PICK_FILE = 1339


const val DEFAULT_RATIO = 1
const val DEFAULT_RATIO_COVER_X = 343
const val DEFAULT_RATIO_COVER_Y = 136
const val DEFAULT_PHOTO_SKIP_CROP = false
const val DEFAULT_MAX_FILE_SIZE_BYTES = 0
const val DEFAULT_MAX_BITMAP_SIZE = 10000
const val DEFAULT_COMPRESSION_QUALITY = 60
const val DEFAULT_COMPRESSION_QUALITY_THUMB = 90
const val DEFAULT_MAX_ZOOM = 10F
const val DEFAULT_MIN_ZOOM = 1F

const val PICK_REQUEST_TYPE_PHOTO = 1
const val PICK_REQUEST_TYPE_VIDEO = 2
const val PICK_REQUEST_TYPE_FILES = 3

const val KEY_ORIGINAL_FILE_NAME = "originalFileName"
const val KEY_FILE_SIZE = "fileSize"
const val KEY_VIDEO_PATH = "videoPath"
const val KEY_MIN_SECONDS = "minSeconds"
const val KEY_MAX_SECONDS = "maxSeconds"
const val KEY_MAX_DISPLAYED_THUMBS = "maxDisplayedThumbs"
const val KEY_COMPRESS_AFTER_TRIM = "compressAfterTrim"
const val KEY_DO_ENCODE = "doEncode"
const val KEY_DO_TRIM = "doTrim"

const val DEBUG_TOUCH_AREA_ENABLED = true

const val DEFAULT_MIN_SECONDS = 1
const val DEFAULT_MIN_DURATION = DEFAULT_MIN_SECONDS * 1000L
const val DEFAULT_MAX_SECONDS = 10
const val DEFAULT_MAX_DURATION = DEFAULT_MAX_SECONDS * 1_000L
const val DEFAULT_MAX_DISPLAYED_THUMBS = 3
const val DEFAULT_COMPRESS_AFTER_TRIM = false
const val DEFAULT_DO_ENCODE = false
const val DEFAULT_KEY_DO_TRIM = false

