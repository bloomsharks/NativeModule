package com.naver.android.helloyako.imagecrop.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.naver.android.helloyako.imagecrop.BitmapDecodeAsync
import com.naver.android.helloyako.imagecrop.DownloadAsync
import tgio.github.com.mediapickerlib.CustomError
import tgio.github.com.mediapickerlib.DEFAULT_MIN_ZOOM
import tgio.github.com.mediapickerlib.Photo
import tgio.github.com.mediapickerlib.R
import java.io.File
import java.util.*

class CropActivity : AppCompatActivity() {

    private lateinit var bloomNativeImageCropView: BloomNativeImageCropView
    private lateinit var photo: Photo
    private lateinit var toolbar: Toolbar
    private lateinit var btnSave: Button

    companion object {
        fun getIntent(context: Context, uri: Uri, request: Photo): Intent {
            return Intent(context, CropActivity::class.java).apply {
                putExtra("uri", uri)
                putExtras(request.toBundle())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bloom_native_activity_crop)


        toolbar = findViewById(R.id.toolbar)
        btnSave = findViewById(R.id.btnSave)

        toolbar.setNavigationOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (intent.extras == null || intent.hasExtra("uri").not()) {
            setResult(
                Activity.RESULT_CANCELED, Intent().putExtras(
                    CustomError(message = "Required params not set.").toBundle()
                )
            )
            finish()
        }

        photo = Photo.fromBundle(intent.extras!!)
        val uri = intent.getParcelableExtra<Uri>("uri")

        photo.nextButtonString?.let {
            btnSave.text = it
        }
        bloomNativeImageCropView = findViewById(R.id.imageCropView)


        getFile(uri)
    }

    private fun getFile(uri: Uri?) {
        PickiT(this, object : PickiTCallbacks {
            override fun PickiTonCompleteListener(
                path: String?,
                wasDriveFile: Boolean,
                wasUnknownProvider: Boolean,
                wasSuccessful: Boolean,
                originalFileName: String,
                originalFileSize: Int,
                Reason: String?
            ) {
                if (wasSuccessful) {
                    bloomNativeImageCropView.setImageFilePath(path)
                    DownloadAsync(
                        this@CropActivity,
                        photo
                    ) { bitmap: Bitmap, ratioX: Int, ratioY: Int ->
                        bloomNativeImageCropView.setAspectRatio(ratioX, ratioY)
                        bloomNativeImageCropView.setImageBitmap(
                            bitmap,
                            DEFAULT_MIN_ZOOM,
                            photo.maxScaleMultiplier
                        )
                    }.execute(Uri.parse(path))
                } else {
                    setResult(
                        Activity.RESULT_CANCELED, Intent().putExtras(
                            CustomError(message = Reason ?: "Unknown").toBundle()
                        )
                    )
                    finish()
                }
                btnSave.setOnClickListener {
                    submitResult()
                }
            }
        }).getPath(uri)
    }

    private fun submitResult() {
        bloomNativeImageCropView.croppedImage?.let { bitmap ->
            val dest = File(this.cacheDir, UUID.randomUUID().toString() + ".jpeg")
            BitmapDecodeAsync(
                context = this@CropActivity,
                bitmap = bitmap,
                photo = photo,
                destination = dest
            ) { file ->
                setResult(
                    Activity.RESULT_OK, Intent()
                        .putExtra("uri", file.toUri())
                        .putExtra("imageWidth", bitmap.width.toString())
                        .putExtra("imageHeight", bitmap.height.toString())
                )
                finish()
            }.execute()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
