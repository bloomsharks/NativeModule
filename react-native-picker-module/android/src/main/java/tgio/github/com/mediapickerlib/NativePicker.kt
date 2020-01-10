package tgio.github.com.mediapickerlib

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
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
import tgio.github.com.mediapickerlib.proxy.Proxy
import java.io.File


class NativePicker(
    private val activity: Activity,
    private val pickMediaRequest: PickMediaRequest,
    private val nativePickerCallback: NativePickerCallback
) {
    init {
        pickMedia()
    }

    private fun launchPhotoPicker() {
        proxy(
            intent = Intent.createChooser(
                Intent(Intent.ACTION_GET_CONTENT, Photo.INTENT_URI)
                    .setType(Photo.INTENT_TYPE)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .putExtra(Intent.EXTRA_MIME_TYPES, Photo.MIME_TYPES),
                "Pick Photo"
            ),
            requestCode = PickMediaRequest.REQUEST_PHOTO
        ) { requestCode, resultCode, data ->
            if (resultCode == Activity.RESULT_OK && requestCode == PickMediaRequest.REQUEST_PHOTO && data?.data != null) {
                launchCrop(data.data!!)
            } else {
                nativePickerCallback.onMediaPickCanceled("Piking photo canceled.")
            }
        }
    }

    private fun proxy(
        intent: Intent,
        requestCode: Int,
        callback: (requestCode: Int, resultCode: Int, data: Intent?) -> Unit
    ) {
        Proxy.with(context = activity.applicationContext)
            .listener { _requestCode, resultCode, data ->
                callback.invoke(_requestCode, resultCode, data)
            }.launch(
                intent,
                requestCode = requestCode
            )
    }

    private fun launchFilesPicker() {
        proxy(
            intent = Intent.createChooser(
                Intent(Intent.ACTION_GET_CONTENT)
                    .setType(Files.INTENT_TYPE),
                "Pick File"
            ),
            requestCode = PickMediaRequest.REQUEST_FILE,
            callback = { _, resultCode, data ->
                if (data?.data == null || resultCode != Activity.RESULT_OK) {
                    nativePickerCallback.onMediaPickCanceled("Picking file canceled.")
                    return@proxy
                }
                PickiT(activity, object : PickiTCallbacks {
                    override fun PickiTonProgressUpdate(progress: Int) {
                        nativePickerCallback.onDownloadProgress(progress)
                    }

                    override fun PickiTonStartListener() {
                        nativePickerCallback.onDownloadProgress(0)
                    }

                    override fun PickiTonCompleteListener(
                        path: String?,
                        wasDriveFile: Boolean,
                        wasUnknownProvider: Boolean,
                        wasSuccessful: Boolean,
                        originalFileName: String,
                        originalFileSize: Int,
                        Reason: String?
                    ) {
                        if (!wasSuccessful) {
                            nativePickerCallback.onMediaPickCanceled(Reason)
                            return
                        }
                        val file = File(path!!)
                        var fileSize = originalFileSize.toString()
                        if (originalFileSize == 0) {
                            fileSize = file.length().toString()
                        }
                        nativePickerCallback.onMediaPicked(
                            PickFileResponse(
                                mediaRequest = pickMediaRequest,
                                uri = path,
                                metadata = PickFileResponse.FileMetaData(
                                    fileName = originalFileName,
                                    fileSizeBytes = fileSize
                                )
                            )
                        )
                    }

                }).also {
                    it.getPath(data.data)
                }
            }
        )
    }

    private fun launchVideoPicker() {
        proxy(
            intent = Intent.createChooser(
                Intent(Intent.ACTION_GET_CONTENT, Video.INTENT_URI)
                    .setType(Video.INTENT_TYPE)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .putExtra(Intent.EXTRA_MIME_TYPES, Video.MIME_TYPES),
                "Pick Video"
            ),
            requestCode = PickMediaRequest.REQUEST_VIDEO,
            callback = { _, resultCode, data ->
                println("pckit data = [${data}]")
                if (data?.data == null || resultCode != Activity.RESULT_OK) {
                    nativePickerCallback.onMediaPickCanceled("Picking video canceled.")
                    return@proxy
                }
                PickiT(activity, object : PickiTCallbacks {
                    override fun PickiTonProgressUpdate(progress: Int) {
                        nativePickerCallback.onDownloadProgress(progress)
                    }

                    override fun PickiTonStartListener() {
                        nativePickerCallback.onDownloadProgress(0)
                    }

                    override fun PickiTonCompleteListener(
                        path: String?,
                        wasDriveFile: Boolean,
                        wasUnknownProvider: Boolean,
                        wasSuccessful: Boolean,
                        originalFileName: String,
                        originalFileSize: Int,
                        Reason: String?
                    ) {
                        if (!wasSuccessful) {
                            nativePickerCallback.onMediaPickCanceled(Reason)
                            return
                        }
                        nativePickerCallback.onMediaPicked(
                            PickVideoResponse(
                                mediaRequest = pickMediaRequest,
                                uri = path!!,
                                metadata = MetaDataUtils.getVideoMetaData(
                                    activity,
                                    filePath = path,
                                    thumbnailQuality = 90,
                                    originalName = originalFileName,
                                    contentUri = data.data
                                )
                            )
                        )
                    }

                }).also {
                    it.getPath(data.data)
                }
            }
        )
    }

    private fun launchCrop(uri: Uri) {
        val fileName = getFileName(uri.lastPathSegment!!)
        val photoRequest = pickMediaRequest as Photo
        CommonUtils.getImageDimensions(activity, uri) { imageDimensions ->
            proxy(
                intent = UCrop.of(
                    uri,
                    Uri.fromFile(File(activity.cacheDir, "cropped$fileName"))
                ).withOptions(getDefaultCropOptions().apply {
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
                }).getIntent(activity),
                requestCode = CROP_IMAGE,
                callback = { _, resultCode, data ->
                    if (resultCode != Activity.RESULT_OK || data == null) {
                        if (data != null) {
                            nativePickerCallback.onMediaPickCanceled(UCrop.getError(data)?.toString())
                        } else {
                            nativePickerCallback.onMediaPickCanceled("Error while cropping image.")
                        }
                        return@proxy
                    }
                    val result = UCrop.getOutput(data)
                    CommonUtils.getImageDimensions(activity, result!!) { imageDimensions ->
                        nativePickerCallback.onMediaPicked(
                            PickPhotoResponse(
                                pickMediaRequest,
                                result.path!!,
                                PickPhotoResponse.PhotoMetaData(
                                    width = imageDimensions.width,
                                    height = imageDimensions.height,
                                    fileName = fileName,
                                    fileSizeBytes = result.toFile().length().toString()
                                )
                            )
                        )
                    }

                }
            )
        }
    }

    private fun getDefaultCropOptions(): UCrop.Options {
        return UCrop.Options().apply {
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

    private fun getFileName(path: String): String {
        return path.substring(path.lastIndexOf("/") + 1)
    }

    companion object {
        const val CROP_IMAGE = 32421
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
                    nativePickerCallback.onMediaPickCanceled("Permission denied.")
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