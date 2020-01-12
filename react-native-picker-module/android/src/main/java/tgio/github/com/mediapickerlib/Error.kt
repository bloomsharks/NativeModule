package tgio.github.com.mediapickerlib

import com.facebook.react.bridge.Promise

enum class Error(val code: Int) {
    CANCELED(0),
    MISSING_OPTIONS(-1),
    PERMISSION_DENIED(-2),
    NULL_ACTIVITY(-3)
}

data class CustomError(
    val code: Int = -1000,
    val message: String = "Unknown"
)

fun Promise.reject(error: CustomError) {
    this.reject(error.code.toString(), error.message)
}
fun Promise.reject(error: Error) {
    this.reject(error.code.toString(), error.name)
}