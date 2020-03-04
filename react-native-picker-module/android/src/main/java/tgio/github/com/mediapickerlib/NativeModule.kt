package tgio.github.com.mediapickerlib

import com.facebook.react.bridge.*
import java.util.*

class NativeModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    var nativePicker: NativePicker? = null

    override fun getName(): String {
        return "PickerModule"
    }

    init {
        deleteOldCachedFiles()
    }

    private fun deleteOldCachedFiles() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        CacheUtils.deleteFiles(reactContext.cacheDir, cal.time)
    }

    @ReactMethod
    fun pickMedia(options: ReadableMap?, promise: Promise) {
        deleteOldCachedFiles()
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
            resolve = { promise.resolve(Arguments.fromBundle(it)) },
            reject = { promise.reject(it) }
        )
    }
}