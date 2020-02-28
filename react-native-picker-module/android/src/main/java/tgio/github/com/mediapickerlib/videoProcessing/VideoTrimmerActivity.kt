package tgio.github.com.mediapickerlib.videoProcessing

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.bloom_native_activity_video_trim.*
import net.protyposis.android.mediaplayer.VideoView
import tgio.github.com.mediapickerlib.*
import tgio.github.com.mediapickerlib.videoProcessing.callbacks.VideoCompressListener
import tgio.github.com.mediapickerlib.videoProcessing.callbacks.VideoTrimListener
import tgio.github.com.mediapickerlib.videoProcessing.widget.RangeSeekBarView
import tgio.github.com.mediapickerlib.videoProcessing.widget.TimelineView
import java.io.File

class VideoTrimmerActivity : AppCompatActivity(R.layout.bloom_native_activity_video_trim) {
    private var mDialog: Dialog? = null

    private lateinit var mLinearVideo: RelativeLayout
    private lateinit var mVideoView: VideoView
    private lateinit var mTimelineView: TimelineView
    private lateinit var ivPlayPause: ImageView
    private lateinit var mRangeSeekBarView: RangeSeekBarView
    private lateinit var mSeekBarLayout: LinearLayout
    private var mSourceUri: Uri? = null


    private var paramMinDuration =
        DEFAULT_MIN_DURATION
    private var paramMaxDuration =
        DEFAULT_MAX_DURATION
    private var paramMaxDisplayedThumbs =
        DEFAULT_MAX_DISPLAYED_THUMBS
    private var paramCompressAfterTrim =
        DEFAULT_COMPRESS_AFTER_TRIM
    private var paramDoEncode =
        DEFAULT_COMPRESS_AFTER_TRIM
    private var paramVideoPath: String? = null

    private lateinit var videoTrimmer: VideoTrimmer

    private val trimListener = object : VideoTrimListener {
        override fun onTrimStart() {
            mDialog = Utils.showProgressDialog(
                this@VideoTrimmerActivity,
                resources.getString(R.string.bloom_native_trimming),
                mDialog
            )
        }

        override fun onTrimError(error: Int, message: String) {
            dismissActiveDialog()
            mDialog = Utils.showErrorDialog(
                this@VideoTrimmerActivity,
                message,
                mDialog
            )
        }

        override fun onTrimFinish(resultPath: String) {
            dismissActiveDialog()
            if (paramCompressAfterTrim) {
                videoTrimmer.compress(resultPath, cacheDir.path, compressListener)
            } else {
                postResult(resultPath, File(resultPath).length())
            }
        }
    }

    private val compressListener = object : VideoCompressListener {
        override fun onCompressStart() {
            mDialog = Utils.showProgressDialog(
                this@VideoTrimmerActivity,
                resources.getString(R.string.bloom_native_compressing),
                mDialog
            )
        }

        override fun onCompressFinish(resultPath: String) {
            dismissActiveDialog()
            postResult(resultPath)
        }

        override fun onCompressError(error: Int, message: String) {
            dismissActiveDialog()
            mDialog = Utils.showErrorDialog(
                this@VideoTrimmerActivity,
                message,
                mDialog
            )
        }
    }

    private fun readParams() {
        with(intent) {
            paramVideoPath = if(hasExtra(KEY_VIDEO_PATH)) {
                getStringExtra(KEY_VIDEO_PATH)
            } else null
            paramMinDuration = if (hasExtra(KEY_MIN_SECONDS)) {
                getIntExtra(
                    KEY_MIN_SECONDS,
                    DEFAULT_MIN_SECONDS
                ) * 1000L
            } else DEFAULT_MIN_DURATION
            paramMaxDuration = if (hasExtra(KEY_MAX_SECONDS)) {
                getIntExtra(
                    KEY_MAX_SECONDS,
                    DEFAULT_MAX_SECONDS
                ) * 1000L
            } else DEFAULT_MAX_DURATION
            paramMaxDisplayedThumbs = if (hasExtra(KEY_MAX_DISPLAYED_THUMBS)) {
                getIntExtra(
                    KEY_MAX_DISPLAYED_THUMBS,
                    DEFAULT_MAX_DISPLAYED_THUMBS
                )
            } else DEFAULT_MAX_DISPLAYED_THUMBS
            paramCompressAfterTrim = if (hasExtra(KEY_COMPRESS_AFTER_TRIM)) {
                getBooleanExtra(
                    KEY_COMPRESS_AFTER_TRIM,
                    DEFAULT_COMPRESS_AFTER_TRIM
                )
            } else DEFAULT_COMPRESS_AFTER_TRIM
            paramDoEncode = if (hasExtra(KEY_DO_ENCODE)) {
                getBooleanExtra(
                    KEY_DO_ENCODE,
                    DEFAULT_DO_ENCODE
                )
            } else DEFAULT_DO_ENCODE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readParams()
        setupViews()
        setUpListeners()

        if (paramVideoPath.isNullOrBlank()) {
            postError("paramVideoPath is missing")
        } else {
            mSourceUri = Uri.parse(paramVideoPath)
        }

        videoTrimmer =
            VideoTrimmer(
                context = this,
                timelineView = mTimelineView,
                videoView = mVideoView,
                rangeSeekBarView = mRangeSeekBarView,
                paramMinDuration = paramMinDuration,
                paramMaxDuration = paramMaxDuration,
                paramMaxDisplayedThumbs = paramMaxDisplayedThumbs,
                videoSource = mSourceUri!!,
                setIsPlaying = ::setIsPlaying,
                setDurationText = ::setDurationText,
                applyVideoViewParams = ::applyVideoViewParams
            )
        videoTrimmer.reset()
    }

    private fun postError(reason: String) {
        setResult(Activity.RESULT_CANCELED, Intent().also {
            it.putExtra("reason", reason)
        })
        finish()
    }

    private fun postResult(resultPath: String, fileSize: Long = 0L) {
        setResult(Activity.RESULT_OK, Intent().also {
            it.data = Uri.parse(resultPath)
            it.putExtra(KEY_ORIGINAL_FILE_NAME, intent.getStringExtra(KEY_ORIGINAL_FILE_NAME))
            it.putExtra("fileSize", fileSize)
            it.putExtra("resultPath", resultPath)
        })
        finish()
    }

    override fun onResume() {
        super.onResume()
        videoTrimmer.onResume()
    }

    public override fun onPause() {
        super.onPause()
        videoTrimmer.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.dismissDialog(mDialog)
    }

    private fun dismissActiveDialog() {
        Utils.dismissDialog(mDialog)
    }

    private fun setupViews() {
        mLinearVideo = findViewById(R.id.layout_surface_view)
        mVideoView = findViewById(R.id.video_loader)
        mTimelineView = findViewById(R.id.timelineView)
        ivPlayPause = findViewById(R.id.ivPlayPause)
        mSeekBarLayout = findViewById(R.id.seekBarLayout)

        mRangeSeekBarView =
            RangeSeekBarView(
                this
            )
        mSeekBarLayout.addView(mRangeSeekBarView)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setDurationText(paramMaxDuration / 1000)
        tvStaticText.text = "You can upload max ${paramMaxDuration / 1000} sec Video"
    }

    private fun setDurationText(millis: Long) {
        tvDuration.text = "Duration ${Utils.convertSecondsToTime(millis / 1000)} min"
    }

    private fun applyVideoViewParams(videoWidth: Int, videoHeight: Int) {
        val lp = mLinearVideo.layoutParams
        val screenHeight = mLinearVideo.height
        val r = videoHeight / videoWidth.toFloat()
        if (videoHeight > videoWidth) {
            lp.height = screenHeight
            overlay.layoutParams.width = mVideoView.width
        } else if(videoHeight < videoWidth) {
            lp.height = (lp.width * r).toInt()
        } else {
            lp.height = mLinearVideo.width
        }
        mLinearVideo.layoutParams = lp
    }

    private fun setUpListeners() {
        ibtnBack.setOnClickListener { onBackPressed() }
        btnReset.setOnClickListener { videoTrimmer.reset() }
        btnSave.setOnClickListener { onSaveClicked() }
        overlay.setOnClickListener { videoTrimmer.playVideoOrPause() }
        btnSave.setOnClickListener { onSaveClicked() }
    }

    private fun onSaveClicked() {
        mVideoView.pause()
        videoTrimmer.trimVideo(
                inputPath = mSourceUri!!.path!!,
                outputPath = cacheDir.path,
                startMs = mRangeSeekBarView.getStartPosition(),
                endMs = mRangeSeekBarView.getEndPosition(),
                doEncode = paramDoEncode,
                listener = trimListener
        )
    }

    private fun setIsPlaying(isPlaying: Boolean) {
        if (isPlaying) {
            ivPlayPause.visibility = View.GONE
            overlay.alpha = 0F
        } else {
            ivPlayPause.visibility = View.VISIBLE
            overlay.alpha = 1F
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?) = true

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun getIntent(
            context: Context,
            videoPath: String,
            request: Video,
            originalFileName: String
        ): Intent {

            return Intent(context, VideoTrimmerActivity::class.java).apply {
                putExtra(KEY_VIDEO_PATH, videoPath)
                putExtra(KEY_ORIGINAL_FILE_NAME, originalFileName)
                putExtras(request.toBundle())
            }
        }
    }
}