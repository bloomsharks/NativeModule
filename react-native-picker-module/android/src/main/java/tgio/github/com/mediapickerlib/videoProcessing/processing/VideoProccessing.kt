package tgio.github.com.mediapickerlib.videoProcessing.processing

import tgio.github.com.mediapickerlib.videoProcessing.callbacks.VideoCompressListener
import tgio.github.com.mediapickerlib.videoProcessing.callbacks.VideoTrimListener
import java.text.SimpleDateFormat
import java.util.*

object VideoProccessing {
    fun trim(
        inputFile: String,
        _outputFile: String,
        startMs: Long,
        endMs: Long,
        encode: Boolean,
        callback: VideoTrimListener
    ) {
        var outputFile = _outputFile
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
        val outputName = "trimmedVideo_$timeStamp.mp4"
        outputFile = "$outputFile/$outputName"
        val start = convertSecondsToTime(startMs / 1000)
        val duration = convertSecondsToTime((endMs - startMs) / 1000)
        val command = if(encode) {
            "-ss $start -i \"$inputFile\" -t $duration -async 1 $outputFile"
        } else {
            "-ss $start -i \"$inputFile\" -t $duration -async 1 -codec copy $outputFile"
        }
        try {
            val tempOutFile = outputFile
            FFmpegExecutor.executeCommand(command, object : FFmpegExecutionListener {
                override fun onCmdStart() {
                    callback.onTrimStart()
                }

                override fun onCmdFinish() {
                    callback.onTrimFinish(tempOutFile)
                }

                override fun onCmdError(error: Int, message: String) {
                    callback.onTrimError(error, message)
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun compress(
        inputFile: String,
        outputFile: String,
        callback: VideoCompressListener
    ) {
        val cmd =
            "-threads 2 -y -i $inputFile -strict -2 -vcodec libx264 -preset ultrafast -crf 28 -acodec copy -ac 2 $outputFile"
        val command = cmd//.split(" ").toTypedArray()
        try {
            FFmpegExecutor.executeCommand(command, object : FFmpegExecutionListener {
                override fun onCmdStart() {
                    callback.onCompressStart()
                }

                override fun onCmdFinish() {
                    callback.onCompressFinish(outputFile)
                }

                override fun onCmdError(error: Int, message: String) {
                    callback.onCompressError(error, message)
                }

            })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun convertSecondsToTime(seconds: Long): String {
        var timeStr: String? = null
        var hour = 0
        var minute = 0
        var second = 0
        if (seconds <= 0) {
            return "00:00"
        } else {
            minute = seconds.toInt() / 60
            if (minute < 60) {
                second = seconds.toInt() % 60
                timeStr =
                    "00:" + unitFormat(
                        minute
                    ) + ":" + unitFormat(
                        second
                    )
            } else {
                hour = minute / 60
                if (hour > 99) return "99:59:59"
                minute %= 60
                second = (seconds - hour * 3600 - minute * 60).toInt()
                timeStr =
                    unitFormat(
                        hour
                    ) + ":" + unitFormat(
                        minute
                    ) + ":" + unitFormat(
                        second
                    )
            }
        }
        return timeStr
    }

    private fun unitFormat(i: Int): String {
        var retStr: String? = null
        retStr = if (i in 0..9) {
            "0$i"
        } else {
            "" + i
        }
        return retStr
    }
}