package tgio.github.com.mediapickerlib

interface NativePickerCallback {
    fun onMediaPicked(pickMediaResponse: PickMediaResponse)
    fun onMediaPickCanceled(reason: String?)
    fun onDownloadProgress(progress: Int)
}