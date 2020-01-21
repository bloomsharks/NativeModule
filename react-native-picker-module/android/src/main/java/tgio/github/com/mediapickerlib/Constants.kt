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