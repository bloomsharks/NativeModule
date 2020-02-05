package tgio.github.com.mediapickerlib.videoProcessing.processing

interface FFmpegExecutionListener {
    fun onCmdStart()
    fun onCmdFinish()
    fun onCmdError(error: Int, message: String)
}