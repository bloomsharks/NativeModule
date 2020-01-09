package tgio.github.com.mediapickerlib

import com.facebook.react.bridge.*

class NativeModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    var nativePicker: NativePicker? = null

    override fun getName(): String {
        return "PickerModule"
    }

    @ReactMethod
    fun pickMedia(options: ReadableMap?, promise: Promise) {
        if(options == null) {
            promise.reject("-1", "options is missing.")
            return
        }
        nativePicker = NativePicker(
            activity = currentActivity!!,
            pickMediaRequest = ObjectMapper.constructMediaPickRequest(options),
            nativePickerCallback = object : NativePickerCallback {
                override fun onMediaPicked(pickMediaResponse: PickMediaResponse) {
                    promise.resolve(ObjectMapper.prepareResponse(pickMediaResponse))
                }

                override fun onMediaPickCanceled(reason: String?) {
                    promise.reject("-1", reason)
                }

                override fun onDownloadProgress(progress: Int) {
                }
            })
    }
}