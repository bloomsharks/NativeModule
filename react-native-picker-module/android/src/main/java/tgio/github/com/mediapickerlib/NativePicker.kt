package tgio.github.com.mediapickerlib

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.naver.android.helloyako.imagecrop.BitmapDecodeAsync
import com.naver.android.helloyako.imagecrop.util.BitmapLoadUtils
import com.naver.android.helloyako.imagecrop.view.CropActivity
import tgio.github.com.mediapickerlib.proxy.Proxy
import tgio.github.com.mediapickerlib.videoProcessing.VideoTrimmerActivity
import java.io.File
import java.util.*

class NativePicker(
    private val activity: Activity,
    private val pickMediaRequest: PickMediaRequest,
    private val resolve: (Bundle) -> Unit,
    private val reject: (Throwable) -> Unit
) {

    init {
        pickMedia()
    }

    private fun proxy(
        intent: Intent,
        requestCode: Int
    ) {
        Proxy.with(context = activity.applicationContext)
            .listener(this::onActivityResult)
            .launch(
                intent = intent,
                requestCode = requestCode
            )
    }

    private fun launchPhotoPicker() = proxy(
        intent = pickMediaRequest.getIntent(),
        requestCode = REQUEST_PICK_PHOTO
    )

    private fun launchFilesPicker() = proxy(
        intent = pickMediaRequest.getIntent(),
        requestCode = REQUEST_PICK_FILE
    )

    private fun launchCrop(uri: Uri) = proxy(
        intent = CropActivity.getIntent(activity, uri, pickMediaRequest as Photo),
        requestCode = REQUEST_CODE_CROP_IMAGE
    )

    private fun launchTrim(videoPath: String, originalFileName: String) = proxy(
        intent = VideoTrimmerActivity.getIntent(
            context = activity,
            videoPath = videoPath,
            request = pickMediaRequest as Video,
            originalFileName = originalFileName
        ),
        requestCode = REQUEST_TRIM_VIDEO
    )

    private fun launchVideoPicker() = proxy(
        intent = pickMediaRequest.getIntent(),
        requestCode = REQUEST_PICK_VIDEO
    )

    private fun handleSkipCropPhotoPick(uri: Uri?) {
        PickiT(activity, object : PickiTCallbacks {
            override fun PickiTonCompleteListener(
                path: String?,
                wasDriveFile: Boolean,
                wasUnknownProvider: Boolean,
                wasSuccessful: Boolean,
                originalFileName: String,
                originalFileSize: Int,
                Reason: String?
            ) {
                BitmapLoadUtils.decode(path, 4000, 4000, false).let { bitmap ->
                    val dest = File(activity.cacheDir, UUID.randomUUID().toString() + ".jpeg")
                    BitmapDecodeAsync(
                        activity,
                        bitmap,
                        pickMediaRequest as Photo,
                        dest
                    ) {
                        PickMediaResponse.handle(
                            context = activity,
                            mediaRequest = pickMediaRequest,
                            intent = Intent().also {
                                it.putExtra("uri", Uri.fromFile(dest))
                                it.putExtra("imageWidth", bitmap.width.toString())
                                it.putExtra("imageHeight", bitmap.height.toString())
                            },
                            resolve = resolve,
                            reject = reject
                        )
                    }.execute()
                }
            }
        }).getPath(uri)
    }

    private fun copyVideo(uri: Uri) {
        PickiT(activity, object : PickiTCallbacks {
            override fun PickiTonCompleteListener(
                path: String?,
                wasDriveFile: Boolean,
                wasUnknownProvider: Boolean,
                wasSuccessful: Boolean,
                originalFileName: String,
                originalFileSize: Int,
                Reason: String?
            ) {
                if(wasSuccessful) {
                    launchTrim(path!!, originalFileName)
                } else {
                    reject.invoke(Error.CANCELED.toThrowable())
                }
            }
        }).getPath(uri)
    }

    private fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fun default() = PickMediaResponse.handle(
            context = activity,
            mediaRequest = pickMediaRequest,
            intent = data,
            resolve = resolve,
            reject = reject
        )
        if (resultCode != Activity.RESULT_OK || data == null) {
            if (data != null) {
                reject.invoke(Error.CANCELED.toThrowable())
            }
            return
        }
        when (requestCode) {
            REQUEST_PICK_PHOTO -> {
                if((pickMediaRequest as Photo).skipCrop) {
                    handleSkipCropPhotoPick(data.data)
                } else {
                    if (resultCode == Activity.RESULT_OK) {
                        launchCrop(data.data!!)
                    } else {
                        reject.invoke(Error.CANCELED.toThrowable())
                    }
                }
            }
            REQUEST_PICK_VIDEO -> {
                if((pickMediaRequest as Video).trim) {
                    if(data.data == null) {
                        reject.invoke(Error.CANCELED.toThrowable())
                    } else {
                        copyVideo(data.data!!)
                    }
                } else {
                    if (resultCode == Activity.RESULT_OK) {
                        default()
                    } else {
                        reject.invoke(Error.CANCELED.toThrowable())
                    }
                }
            }
            REQUEST_PICK_FILE,
            REQUEST_TRIM_VIDEO,
            REQUEST_CODE_CROP_IMAGE ->  default()
        }
    }

    private fun pickMedia() {
        Dexter.withActivity(activity)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    when (pickMediaRequest) {
                        is Photo -> launchPhotoPicker()
                        is Video -> launchVideoPicker()
                        is Files -> launchFilesPicker()
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    reject.invoke(Error.PERMISSION_DENIED.toThrowable())
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }
}
