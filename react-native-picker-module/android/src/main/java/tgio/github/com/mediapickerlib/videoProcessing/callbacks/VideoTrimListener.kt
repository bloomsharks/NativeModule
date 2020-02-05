package tgio.github.com.mediapickerlib.videoProcessing.callbacks

interface VideoTrimListener {
    fun onTrimStart()
    fun onTrimFinish(resultPath: String)
    fun onTrimError(error: Int, message: String)
}