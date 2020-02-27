package tgio.github.com.mediapickerlib

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import java.io.File

 class NativeActivity : AppCompatActivity(R.layout.activity_native),
    View.OnClickListener {

    private lateinit var ivResult: ImageView
    private lateinit var tvResultUri: TextView
    private lateinit var btnPickPhoto: Button
    private lateinit var btnPickCover: Button
    private lateinit var btnPickVideo: Button
    private lateinit var btnPickFile: Button
    private lateinit var btnPickPost: Button
    private lateinit var btnPickCustomRatioPhoto: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnPickPhoto = findViewById(R.id.btnPickPhoto)
        btnPickCover = findViewById(R.id.btnPickCover)
        btnPickVideo = findViewById(R.id.btnPickVideo)
        btnPickFile = findViewById(R.id.btnPickFile)
        ivResult = findViewById(R.id.ivResult)
        tvResultUri = findViewById(R.id.tvResultUri)
        btnPickPost = findViewById(R.id.btnPickPost)
        btnPickCustomRatioPhoto = findViewById(R.id.btnPickCustomRatioPhoto)


        btnPickPhoto.setOnClickListener(this)
        btnPickCover.setOnClickListener(this)
        btnPickFile.setOnClickListener(this)
        btnPickVideo.setOnClickListener(this)
        btnPickPost.setOnClickListener(this)
        btnPickCustomRatioPhoto.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        tvResultUri.text = ""
        Glide.with(this).clear(ivResult)

        when (v) {
            btnPickPhoto -> {
                NativePicker(
                    this,
                    Photo(
                        ratioX = 1,
                        ratioY = 1,
                        maxBitmapSize = 10000,
                        maxScaleMultiplier = 10F,
                        maxFileSizeBytes = 0,
                        compressionQuality = 60
                    ),
                    resolve = ::resolveCallback,
                    reject = ::rejectCallback
                )
            }
            btnPickCover -> {
                NativePicker(
                    this,
                    Photo(
                        ratioX = 343,
                        ratioY = 136,
                        maxBitmapSize = 10000,
                        maxScaleMultiplier = 10F,
                        maxFileSizeBytes = 0,
                        compressionQuality = 60
                    ),
                    resolve = ::resolveCallback,
                    reject = ::rejectCallback
                )
            }
            btnPickVideo -> {
                NativePicker(
                    this,
                    Video(
                        trim = true,
                        compress = false,
                        encode = false,
                        minDurationSeconds = 1,
                        maxDurationSeconds = 50
                    ),
                    resolve = ::resolveCallback,
                    reject = ::rejectCallback
                )
            }
            btnPickFile -> {
                NativePicker(
                    this,
                    Files(),
                    resolve = ::resolveCallback,
                    reject = ::rejectCallback
                )
            }
            btnPickCustomRatioPhoto -> {
                NativePicker(
                    this,
                    Photo(
                        ratioX = 5760,
                        ratioY = 3840,
                        maxBitmapSize = 10000,
                        maxScaleMultiplier = 10F,
                        maxFileSizeBytes = 8 * 1024 * 1024,
                        compressionQuality = 60,
                        skipCrop = true
                    ),
                    resolve = ::resolveCallback,
                    reject = ::rejectCallback
                )
            }
            btnPickPost -> {
                NativePicker(
                    this,
                    Photo(
                        ratioX = 0,
                        ratioY = 0,
                        maxBitmapSize = 10000,
                        maxScaleMultiplier = 10F,
                        maxFileSizeBytes = 0,
                        compressionQuality = 60
                    ),
                    resolve = ::resolveCallback,
                    reject = ::rejectCallback
                )
            }
        }
    }

    private fun resolveCallback(result: Bundle) {
        println("promise resolve result = [${result}]")
        onMediaPicked(result)
    }

    private fun rejectCallback(throwable: Throwable) {
        println("promise reject throwable = [${throwable}]")
        tvResultUri.text = throwable.toString()
    }

    private fun onMediaPicked(pickMediaResponse: Bundle) {
        runOnUiThread {
            tvResultUri.text = pickMediaResponse.toString()
//            val hr = TempUtils.humanReadableByteCountBin(pickMediaResponse.getString("fileSize")!!.toLong())

            if (pickMediaResponse.containsKey("thumbnail")) {
                val thumbnail = pickMediaResponse.getString("thumbnail")
                if (thumbnail.isNullOrBlank().not()) {
                    Glide.with(this)
                        .load(thumbnail)
                        .into(ivResult)
                }
            }
            Glide.with(this)
                .load(pickMediaResponse.getString("uri"))
                .into(ivResult)

            consumeResult(pickMediaResponse.getString("uri")!!, pickMediaResponse.getString("type")!!)
        }
    }

    private fun consumeResult(uri: String, type: String) {
        val isInternal = uri.startsWith("data")
        if(isInternal.not()) {
            return
        }

        val videoFile = File(uri)
        val intent = Intent(Intent.ACTION_VIEW)

        val fileUri = FileProvider.getUriForFile(this, "tgio.github.com.mediapickerlib.fileprovider", videoFile)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(fileUri, type)
        startActivity(intent)
    }
}
