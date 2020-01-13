package tgio.github.com.mediapickerlib

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Size
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.facebook.react.bridge.Promise
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import tgio.github.com.mediapickerlib.MetaDataUtils.getFileNameAndSize
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
        val fileName = CommonUtils.getFileName(uri.lastPathSegment!!)
        val photoRequest = pickMediaRequest as Photo
        CommonUtils.getImageDimensions(activity, uri) { imageDimensions ->
            proxy(
                intent = UCrop.of(
                    uri,
                    Uri.fromFile(File(activity.cacheDir, fileName))
                ).withOptions(getDefaultCropOptions(photoRequest, imageDimensions))
                    .getIntent(activity),
                requestCode = REQUEST_CODE_CROP_IMAGE
            )
        }
    }

    private fun launchVideoPicker() = proxy(
        intent = pickMediaRequest.getIntent(),
        requestCode = REQUEST_PICK_VIDEO
    )

    private fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            if (data != null) {
                promise.reject(UCrop.getError(data))
            } else {
                promise.reject(Error.CANCELED)
            }
            return
        }
        when (requestCode) {
            REQUEST_PICK_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    launchCrop(data.data!!)
                } else {
                    promise.reject(Error.CANCELED)
                }
            }
            REQUEST_PICK_FILE -> {
                val result = data.data
                if(result == null) {
                    promise.reject(Error.CANCELED)
                } else {
                    val (size, name) = getFileNameAndSize(activity, result)
                    promise.resolve(
                        ObjectMapper.prepareResponse(
                            PickFileResponse(
                                mediaRequest = pickMediaRequest,
                                uri = result.toString(),
                                metadata = PickFileResponse.FileMetaData(
                                    fileName = name,
                                    fileSizeBytes = size
                                )
                            )
                        )
                    )
                }
            }
            REQUEST_PICK_VIDEO -> {
                val result = data.data
                if(result == null) {
                    promise.reject(Error.CANCELED)
                } else {
                    val (size, name) = getFileNameAndSize(activity, result)
                    promise.resolve(
                        ObjectMapper.prepareResponse(
                            PickVideoResponse(
                                mediaRequest = pickMediaRequest,
                                uri = result.toString(),
                                metadata = MetaDataUtils.getVideoMetaData(
                                    activity,
                                    filePath = result,
                                    thumbnailQuality = 90,
                                    originalName = name,
                                    fileSizeBytes = size
                                )
                            )
                        )
                    )
                }
            }
            REQUEST_CODE_CROP_IMAGE -> {
                val result = UCrop.getOutput(data)
                val (size, name) = getFileNameAndSize(activity, result!!)
                promise.resolve(
                    ObjectMapper.prepareResponse(
                        PickPhotoResponse(
                            mediaRequest = pickMediaRequest,
                            uri = result.toString(),
                            metadata = PickPhotoResponse.PhotoMetaData(
                                width = UCrop.getOutputImageWidth(data),
                                height = UCrop.getOutputImageHeight(data),
                                fileName = name,
                                fileSizeBytes = size
                            )
                        )
                    )
                )
            }
        }
    }

    private fun getDefaultCropOptions(
        photoRequest: Photo,
        imageDimensions: Size
    ): UCrop.Options {
        return UCrop.Options().apply {
            photoRequest.nextButtonString?.let {
                setNextButtonText(it)
            }

            if (photoRequest.proportion == Photo.Proportion.POST) {
                when {
                    imageDimensions.width > imageDimensions.height -> {
                        //if Landscape
                        withAspectRatio(4F, 3F)
                    }
                    imageDimensions.width < imageDimensions.height -> {
                        //if Portrait
                        withAspectRatio(3F, 4F)
                    }
                    else -> {
                        //else Original
                        useSourceImageAspectRatio()
                    }
                }
            } else {
                withAspectRatio(photoRequest.proportion.x, photoRequest.proportion.y)
            }

            setMaxScaleMultiplier(photoRequest.maxScaleMultiplier)
            setCompressionQuality(photoRequest.compressionQuality)
            setMaxSizeBytes(photoRequest.maxFileSizeBytes)
            setMaxBitmapSize(photoRequest.maxBitmapSize)

            setShowCropGrid(false)
            setToolbarTitle("Crop")
            setNextButtonText("Done")
            setLogoColor(Color.WHITE)
            setCropFrameStrokeWidth(3)
            setHideBottomControls(true)
            setFreeStyleCropEnabled(false)
            setRootViewBackgroundColor(Color.WHITE)
            setDimmedLayerColor(Color.parseColor("#CCFFFFFF"))
            setCropFrameColor(ContextCompat.getColor(activity, R.color.accent))
            setToolbarWidgetColor(ContextCompat.getColor(activity, R.color.ucrop_color_black))
            setAllowedGestures(UCropActivity.SCALE, UCropActivity.SCALE, UCropActivity.SCALE)
            setActiveControlsWidgetColor(ContextCompat.getColor(activity, R.color.accent))
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