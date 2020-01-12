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
        if(currentActivity == null) {
            promise.reject(Error.NULL_ACTIVITY)
            return
        }
        if(options == null) {
            promise.reject(Error.MISSING_OPTIONS)
            return
        }
        val pickMediaRequest = try {
            ObjectMapper.constructMediaPickRequest(options)
        } catch (e: Exception) {
            promise.reject(CustomError(message = e.message ?: "Unknown"))
            return
        }
        nativePicker = NativePicker(
            activity = currentActivity!!,
            pickMediaRequest = pickMediaRequest,
            promise = promise)
    }
}