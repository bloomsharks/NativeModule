package tgio.github.com.mediapickerlib

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.facebook.react.bridge.Promise
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
import java.io.File

class NativePicker(
    private val activity: Activity,
    private val pickMediaRequest: PickMediaRequest,
    private val promise: Promise
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

    private fun launchCrop(uri: Uri) {
        println("QWW uri = [${uri}]")
        proxy(
            intent = CropActivity.getIntent(activity, uri, pickMediaRequest as Photo),
            requestCode = REQUEST_CODE_CROP_IMAGE
        )
    }

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
                    val dest = File(activity.cacheDir, originalFileName)
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
                            promise = promise
                        )
                    }.execute()
                }
            }
        }).getPath(uri)
    }


    private fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("QWW requestCode = [${requestCode}], resultCode = [${resultCode}], data = [${data}]")
        if (resultCode != Activity.RESULT_OK || data == null) {
            if (data != null) {
                promise.reject(Error.CANCELED)
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
                        promise.reject(Error.CANCELED)
                    }
                }
            }
            REQUEST_PICK_FILE,
            REQUEST_PICK_VIDEO,
            REQUEST_CODE_CROP_IMAGE -> {
                PickMediaResponse.handle(
                    context = activity,
                    mediaRequest = pickMediaRequest,
                    intent = data,
                    promise = promise
                )
            }
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
                    promise.reject(Error.PERMISSION_DENIED)
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