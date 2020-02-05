package tgio.github.com.mediapickerlib.videoProcessing.callbacks

interface VideoCompressListener {
    fun onCompressStart()
    fun onCompressFinish(resultPath: String)
    fun onCompressError(error: Int, message: String)
}