//Option "mediaType"
export const MediaTypePhoto = 'photo';
export const MediaTypeVideo = 'video';
export const MediaTypeFile = 'file';

//Option "proportion"
export const PHOTO_PROFILE = 'profile';
export const PHOTO_COVER = 'cover';
export const PHOTO_POST = 'post';
export const PHOTO_CUSTOM = 'custom';

//Option for custom photo ratio [FLOAT]
export const PHOTO_X = 'keyX';
export const PHOTO_Y = 'keyY';

//If value is more than 0, it will ignore KEY_PHOTO_COMPRESSION_QUALITY
//and use backing compression, starting from 100 down to 0, if result
//size exceeds given number [INT]
//default 0
export const PHOTO_MAX_FILE_SIZE_BYTES = 'maxFileSizeBytes';

//[String]
export const NEXT_BUTTON_STRING = 'nextButtonString';

//number, from 5 to 100 [INT]
//default 60
export const PHOTO_COMPRESSION_QUALITY = 'compressionQuality';

//max zoom from 1.0 to 10.0 [FLOAT]
//default 10.0
export const PHOTO_MAX_SCALE_MULTIPLIER = 'maxScaleMultiplier';

//ax size for both width and height of bitmap that will be
// decoded from an input Uri and used in the view [INT]
//default 10000
export const PHOTO_MAX_BITMAP_SIZE = 'maxBitmapSize';
