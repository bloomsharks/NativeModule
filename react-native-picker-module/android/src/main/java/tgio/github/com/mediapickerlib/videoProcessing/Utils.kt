package tgio.github.com.mediapickerlib.videoProcessing

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AlertDialog

object Utils {

    @Suppress("DEPRECATION")
    fun showProgressDialog(context: Context, msg: String, previousDialog: Dialog?): ProgressDialog {
        dismissDialog(previousDialog)
        val mProgressDialog = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        mProgressDialog.setMessage(msg)
        mProgressDialog.setCancelable(false)
        mProgressDialog.show()
        return mProgressDialog
    }

    fun showErrorDialog(
        context: Context,
        msg: String,
        previousDialog: Dialog?
    ): AlertDialog {
        dismissDialog(previousDialog)
        val dialog = AlertDialog.Builder(context)
        dialog.setMessage(msg)
        dialog.setTitle("Error")
        dialog.setCancelable(false)
        dialog.setPositiveButton("OK") { d, _ -> d?.dismiss() }
        return dialog.show()
    }

    fun dismissDialog(dialog: Dialog?) {
        dialog?.let {
            if (it.isShowing && it is ProgressDialog) {
                it.dismiss()
            }
        }
    }

    fun convertSecondsToTime(seconds: Long, delimiter: String = "."): String {
        var timeStr: String? = null
        var hour = 0
        var minute = 0
        var second = 0
        if (seconds <= 0) {
            return "0${delimiter}00"
        } else {
            minute = seconds.toInt() / 60
            if (minute < 60) {
                second = seconds.toInt() % 60
                timeStr = minute.toString() + delimiter + unitFormat(second)
            } else {
                hour = minute / 60
                if (hour > 99) return "99${delimiter}59${delimiter}59"
                minute %= 60
                second = (seconds - hour * 3600 - minute * 60).toInt()
                timeStr = hour.toString() + delimiter + unitFormat(minute) + delimiter + unitFormat(second)
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