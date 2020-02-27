package tgio.github.com.mediapickerlib.videoProcessing.proccessing

interface FFmpegExecutionListener {
    fun onCmdStart()
    fun onCmdFinish()
    fun onCmdError(error: Int, message: String)
}