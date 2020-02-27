package tgio.github.com.mediapickerlib.videoProcessing.proccessing

import android.os.AsyncTask
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Level
import com.arthenica.mobileffmpeg.util.AsyncSingleFFmpegExecuteTask

object FFmpegExecutor {
    fun executeCommand(cmd: String, callback: FFmpegExecutionListener) {
        var errorMessage = ""
        Config.enableLogCallback {
            if(it.level < Level.AV_LOG_ERROR) {
                errorMessage += it.text
            }
        }
        AsyncSingleFFmpegExecuteTask(cmd) { returnCode, executeOutput ->
            if (returnCode == 0) {
                callback.onCmdFinish()
            } else {
                callback.onCmdError(returnCode, errorMessage)
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        callback.onCmdStart()
    }
}