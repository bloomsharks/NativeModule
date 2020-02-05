package tgio.github.com.mediapickerlib.videoProcessing

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bloom_native_activity_video_trim.*
import tgio.github.com.mediapickerlib.*
import tgio.github.com.mediapickerlib.videoProcessing.callbacks.VideoCompressListener
import tgio.github.com.mediapickerlib.videoProcessing.callbacks.VideoTrimListener
import tgio.github.com.mediapickerlib.videoProcessing.processing.VideoProccessing
import tgio.github.com.mediapickerlib.videoProcessing.widget.RangeSeekBarView
import tgio.github.com.mediapickerlib.videoProcessing.widget.SpacesItemDecoration2
import tgio.github.com.mediapickerlib.videoProcessing.widget.VideoTrimmerAdapter
import tgio.github.com.mediapickerlib.videoProcessing.widget.ZVideoView
import java.io.File
import kotlin.math.abs

class VideoTrimmerActivity : AppCompatActivity() {
    private var mDialog: Dialog? = null

    private var screenWidth = 0
    private var horizontalPadding = 0
    private var videoFramesWidth = 0
    private var mMaxWidth = 0

    private lateinit var mLinearVideo: RelativeLayout
    private lateinit var mVideoView: ZVideoView
    private lateinit var ivPlayPause: ImageView
    private lateinit var mVideoThumbRecyclerView: RecyclerView
    private lateinit var mRangeSeekBarView: RangeSeekBarView
    private lateinit var mSeekBarLayout: LinearLayout
    private lateinit var mRedProgressIcon: ImageView
    private var mAverageMsPx = 0f
    private var averagePxMs = 0f
    private var mSourceUri: Uri? = null
    private var mDuration = 0
    private var mVideoThumbAdapter: VideoTrimmerAdapter? = null
    var restoreState = false
    private var mLeftProgressPos: Long = 0
    private var mRightProgressPos: Long = 0
    private var mRedProgressBarPos: Long = 0
    private var scrollPos: Long = 0
    private val mScaledTouchSlop = 0
    private var lastScrollX = 0
    private var isSeeking = false
    private var isOverScaledTouchSlop = false
    private var mThumbsTotalCount = 0
    private var mRedProgressAnimator: ValueAnimator? = null
    private val mAnimationHandler = Handler()

    private var paramMinDuration = DEFAULT_MIN_DURATION
    private var paramMaxDuration = DEFAULT_MAX_DURATION
    private var paramMaxDisplayedThumbs = DEFAULT_MAX_DISPLAYED_THUMBS
    private var paramCompressAfterTrim = DEFAULT_COMPRESS_AFTER_TRIM
    private var paramDoEncode = DEFAULT_COMPRESS_AFTER_TRIM
    private var paramStaticText = DEFAULT_STATIC_TEXT
    private var paramVideoPath: String? = null

    private fun setDimens() {
        screenWidth = resources.displayMetrics.widthPixels
        horizontalPadding = resources.getDimensionPixelOffset(R.dimen.bloom_native_paddingTimeline) + resources.getDimensionPixelOffset(R.dimen.bloom_native_thumb_width)
        videoFramesWidth = screenWidth - horizontalPadding * 2
        mMaxWidth = videoFramesWidth
    }

    private fun readParams() {
        with(intent) {
            paramVideoPath = if(hasExtra(KEY_VIDEO_PATH)) {
                getStringExtra(KEY_VIDEO_PATH)
            } else null
            paramMinDuration = if (hasExtra(KEY_MIN_SECONDS)) {
                getIntExtra(KEY_MIN_SECONDS, DEFAULT_MIN_SECONDS) * 1000L
            } else DEFAULT_MIN_DURATION
            paramMaxDuration = if (hasExtra(KEY_MAX_SECONDS)) {
                getIntExtra(KEY_MAX_SECONDS, DEFAULT_MAX_SECONDS) * 1000L
            } else DEFAULT_MAX_DURATION
            paramMaxDisplayedThumbs = if (hasExtra(KEY_MAX_DISPLAYED_THUMBS)) {
                getIntExtra(KEY_MAX_DISPLAYED_THUMBS, DEFAULT_MAX_DISPLAYED_THUMBS)
            } else DEFAULT_MAX_DISPLAYED_THUMBS
            paramCompressAfterTrim = if (hasExtra(KEY_COMPRESS_AFTER_TRIM)) {
                getBooleanExtra(KEY_COMPRESS_AFTER_TRIM, DEFAULT_COMPRESS_AFTER_TRIM)
            } else DEFAULT_COMPRESS_AFTER_TRIM
            paramDoEncode = if (hasExtra(KEY_DO_ENCODE)) {
                getBooleanExtra(KEY_DO_ENCODE, DEFAULT_DO_ENCODE)
            } else DEFAULT_DO_ENCODE
            paramStaticText = if (hasExtra(KEY_STATIC_TEXT)) {
                getStringExtra(KEY_STATIC_TEXT) ?: DEFAULT_STATIC_TEXT
            } else DEFAULT_STATIC_TEXT
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setDimens()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bloom_native_activity_video_trim)

        readParams()

        setupViews()


        if (paramVideoPath.isNullOrBlank()) {
            postError("paramVideoPath is missing")
        } else {
            initVideoByURI(Uri.parse(paramVideoPath))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED, Intent().also {
            it.putExtra("reason", "canceled")
        })
    }
    private fun postError(reason: String) {
        setResult(Activity.RESULT_CANCELED, Intent().also {
            it.putExtra("reason", reason)
        })
        finish()
    }

    private fun postResult(resultPath: String) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(KEY_ORIGINAL_FILE_NAME, intent.getStringExtra(KEY_ORIGINAL_FILE_NAME))
            }.setData(Uri.parse("file://$resultPath"))
        )
        finish()
    }

    public override fun onPause() {
        super.onPause()
        onVideoPause()
        restoreState = true
    }

    private fun trimVideo() {
        VideoProccessing.trim(
            inputFile = mSourceUri!!.path!!,
            _outputFile = cacheDir.path,
            startMs = mRangeSeekBarView.getStartPosition() * 1000,
            endMs = mRangeSeekBarView.getEndPosition() * 1000,
            encode = paramDoEncode,
            callback = object : VideoTrimListener {
                override fun onTrimStart() {
                    mDialog = Utils.showProgressDialog(
                        this@VideoTrimmerActivity,
                        resources.getString(R.string.bloom_native_trimming),
                        mDialog
                    )
                }

                override fun onTrimError(error: Int, message: String) {
                    dismissActiveDialog()
                    mDialog = Utils.showErrorDialog(this@VideoTrimmerActivity, message, mDialog)
                }

                override fun onTrimFinish(resultPath: String) {
                    dismissActiveDialog()
                    val out = cacheDir.path + File.separator + COMPRESSED_VIDEO_FILE_NAME
                    if (paramCompressAfterTrim) {
                        compress(resultPath, out)
                    } else {
                        postResult(resultPath)
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.dismissDialog(mDialog)
    }

    private fun dismissActiveDialog() {
        Utils.dismissDialog(mDialog)
    }

    private fun compress(sourcePath: String, outputPath: String) {
        VideoProccessing.compress(sourcePath, outputPath, object : VideoCompressListener {
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
//                finish()
            }

            override fun onCompressError(error: Int, message: String) {
                dismissActiveDialog()
                mDialog = Utils.showErrorDialog(this@VideoTrimmerActivity, message, mDialog)
            }
        })
    }

    private fun setupViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        mLinearVideo = findViewById(R.id.layout_surface_view)
        mVideoView = findViewById(R.id.video_loader)
        ivPlayPause = findViewById(R.id.ivPlayPause)
        mSeekBarLayout = findViewById(R.id.seekBarLayout)
        mRedProgressIcon = findViewById(R.id.positionIcon)
        mVideoThumbRecyclerView = findViewById(R.id.video_frames_recyclerView)
        mVideoThumbRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        mVideoThumbAdapter = VideoTrimmerAdapter(
            this,
            videoFramesWidth / paramMaxDisplayedThumbs
        )
        mVideoThumbRecyclerView.adapter = mVideoThumbAdapter
        mVideoThumbRecyclerView.addOnScrollListener(mOnScrollListener)
        setUpListeners()
        setPlayProgressPosition(getPlayingProgressPosition())
        setDurationText(paramMaxDuration)
        tvStaticText.text = paramStaticText
    }

    private fun setDurationText(duration: Long = paramMaxDuration) {
        tvDuration.text = "Duration ${Utils.convertSecondsToTime(duration / 1000)} min"
    }

    private fun initRangeSeekBarView() {
        if (::mRangeSeekBarView.isInitialized) return
        val rangeWidth: Int
        mLeftProgressPos = 0
        if (mDuration <= paramMaxDuration) {
            mThumbsTotalCount = paramMaxDisplayedThumbs
            rangeWidth = mMaxWidth
            mRightProgressPos = mDuration.toLong()
            paramMaxDuration = mDuration.toLong()
            setDurationText()
        } else {
            mThumbsTotalCount =
                (mDuration * 1.0f / (paramMaxDuration * 1.0f) * paramMaxDisplayedThumbs).toInt()
            rangeWidth = mMaxWidth / paramMaxDisplayedThumbs * mThumbsTotalCount
            mRightProgressPos = paramMaxDuration
        }
        mVideoThumbRecyclerView.addItemDecoration(
            SpacesItemDecoration2(
                horizontalPadding,
                mThumbsTotalCount
            )
        )
        mRangeSeekBarView = RangeSeekBarView(
            context = this,
            mLeftProgressPos = mLeftProgressPos,
            mRightProgressPos = mRightProgressPos
        )
        mRangeSeekBarView.setMinDuration(paramMinDuration)
        mRangeSeekBarView.selectedMinValue = mLeftProgressPos
        mRangeSeekBarView.selectedMaxValue = mRightProgressPos
        mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
        mRangeSeekBarView.setMinShootTime(paramMinDuration)
        mRangeSeekBarView.isNotifyWhileDragging = true
        mRangeSeekBarView.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener)
        mSeekBarLayout.addView(mRangeSeekBarView)
        mAverageMsPx = mDuration * 1.0f / rangeWidth * 1.0f
        averagePxMs = mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos)
        val length = mRangeSeekBarView.getEndPosition() - mRangeSeekBarView.getStartPosition()
        tvDuration.text = "Duration ${Utils.convertSecondsToTime(length)} min"
    }

    fun initVideoByURI(videoURI: Uri) {
        mSourceUri = videoURI
        mVideoView.setVideoURI(mSourceUri!!)
        mVideoView.requestFocus()
    }

    private fun videoPrepared(mp: MediaPlayer) {
        val lp = mVideoView.layoutParams
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight
        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth = mLinearVideo.width
        val screenHeight = mLinearVideo.height
        if (videoHeight > videoWidth) {
            lp.width = screenWidth
            lp.height = screenHeight
//            (mLinearVideo.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H, ${lp.width},${lp.height}"
        } else {
            lp.width = screenWidth
            val r = videoHeight / videoWidth.toFloat()
            lp.height = (lp.width * r).toInt()
//            (mLinearVideo.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H, ${lp.width},${r}"
        }
        mVideoView.layoutParams = lp
        mDuration = mVideoView.duration
        if (!restoreState) {
            seekTo(mRedProgressBarPos)
        } else {
            restoreState = false
            seekTo(mRedProgressBarPos)
        }
        initRangeSeekBarView()
        mVideoThumbAdapter!!.setData(mDuration.toLong(), mThumbsTotalCount, mSourceUri!!.path!!)
    }

    private fun videoCompleted() {
        seekTo(mLeftProgressPos)
        setPlayPauseViewIcon(false)
        setPlayProgressPosition(getPlayingProgressPosition())
    }

    private fun getPlayingProgressPosition(): Int {
        return (horizontalPadding + (mRedProgressBarPos - scrollPos) * averagePxMs).toInt()
    }

    private fun onVideoReset() {
        mVideoView.pause()
        mVideoView.seekTo(0)
        pauseRedProgressAnimation()
        setPlayPauseViewIcon(false)
        mLeftProgressPos = 0
        mRedProgressBarPos = 0
        scrollPos = 0
        mRightProgressPos = paramMaxDuration
        mRangeSeekBarView.reset()
        mRangeSeekBarView.selectedMinValue = 0
        mRangeSeekBarView.selectedMaxValue = paramMaxDuration
        mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
        averagePxMs = mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos)
        mRangeSeekBarView.invalidate()

        mVideoThumbRecyclerView.scrollToPosition(0)

        setDurationText()
        setPlayProgressPosition(getPlayingProgressPosition())
    }

    private fun playVideoOrPause() {
        mRedProgressBarPos = mVideoView.currentPosition.toLong()
        if (mVideoView.isPlaying) {
            mVideoView.pause()
            pauseRedProgressAnimation()
        } else {
            mVideoView.start()
            playingRedProgressAnimation()
        }
        setPlayPauseViewIcon(mVideoView.isPlaying)
    }

    fun onVideoPause() {
        if (mVideoView.isPlaying) {
            seekTo(mLeftProgressPos)
            mVideoView.pause()
            setPlayPauseViewIcon(false)
        }
    }


    private fun setUpListeners() {
        btnReset.setOnClickListener { onVideoReset() }
        btnSave.setOnClickListener { onSaveClicked() }
        mVideoView.setOnPreparedListener { mp ->
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            videoPrepared(mp)
        }
        mVideoView.setOnCompletionListener { videoCompleted() }
        overlay.setOnClickListener { playVideoOrPause() }
        btnSave.setOnClickListener {
            onSaveClicked()
        }
    }

    private fun onSaveClicked() {
        if (mRightProgressPos - mLeftProgressPos < paramMinDuration) {
            Toast.makeText(this, "Error, too short.", Toast.LENGTH_SHORT).show()
        } else {
            mVideoView.pause()
            trimVideo()
        }
    }

    private fun seekTo(msec: Long) {
        mVideoView.seekTo(msec.toInt())
    }

    private fun setPlayPauseViewIcon(isPlaying: Boolean) {
        ivPlayPause.setImageResource(
            if (isPlaying)
                R.drawable.bloom_native_ic_pause
            else
                R.drawable.bloom_native_ic_play_arrow
        )
    }

    private val mOnRangeSeekBarChangeListener: RangeSeekBarView.OnRangeSeekBarChangeListener =
        object : RangeSeekBarView.OnRangeSeekBarChangeListener {
            override fun onRangeSeekBarValuesChanged(
                bar: RangeSeekBarView?,
                minValue: Long,
                maxValue: Long,
                action: Int,
                isMin: Boolean,
                pressedThumb: RangeSeekBarView.Thumb?
            ) {
                mLeftProgressPos = minValue + scrollPos
                mRedProgressBarPos = mLeftProgressPos
                mRightProgressPos = maxValue + scrollPos
                when (action) {
                    MotionEvent.ACTION_DOWN -> isSeeking = false
                    MotionEvent.ACTION_MOVE -> {
                        isSeeking = true
                        seekTo(if (pressedThumb == RangeSeekBarView.Thumb.MIN) mLeftProgressPos else mRightProgressPos)
                    }
                    MotionEvent.ACTION_UP -> {
                        isSeeking = false
                        seekTo(mLeftProgressPos)
                    }
                    else -> {
                    }
                }
                mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)

                if (isSeeking) {
                    setPlayProgressPosition(getPlayingProgressPosition())
                    pauseRedProgressAnimation()
                }

                val length = mRangeSeekBarView.getEndPosition() - mRangeSeekBarView.getStartPosition()
                tvDuration.text = "Duration ${Utils.convertSecondsToTime(length)} min"
            }
        }


    private val mOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                isSeeking = false
                val scrollX = calcScrollXDistance()
                if (abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                    isOverScaledTouchSlop = false
                    return
                }
                isOverScaledTouchSlop = true
                if (scrollX == -horizontalPadding) {
                    scrollPos = 0
                } else {
                    isSeeking = true
                    scrollPos =
                        (mAverageMsPx * (horizontalPadding + scrollX)).toLong()
                    mLeftProgressPos = mRangeSeekBarView.selectedMinValue + scrollPos
                    mRightProgressPos = mRangeSeekBarView.selectedMaxValue + scrollPos
                    mRedProgressBarPos = mLeftProgressPos

                    setPlayProgressPosition(getPlayingProgressPosition())
                    pauseRedProgressAnimation()

                    mVideoView.pause()
                    setPlayPauseViewIcon(false)

                    seekTo(mLeftProgressPos)
                    mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
                    mRangeSeekBarView.invalidate()
                }
                lastScrollX = scrollX
            }
        }

    private fun calcScrollXDistance(): Int {
        val layoutManager = mVideoThumbRecyclerView.layoutManager as LinearLayoutManager?
        val position = layoutManager!!.findFirstVisibleItemPosition()
        val firstVisibleChildView = layoutManager.findViewByPosition(position)
        val itemWidth = firstVisibleChildView!!.width
        return position * itemWidth - firstVisibleChildView.left
    }

    private fun playingRedProgressAnimation() {
        pauseRedProgressAnimation()
        playingAnimation()
        mAnimationHandler.post(mAnimationRunnable)
    }

    private fun playingAnimation() {
        val w2 = resources.getDimensionPixelOffset(R.dimen.bloom_native_progress_thumb_width)
        val end = (horizontalPadding + (mRightProgressPos - scrollPos) * averagePxMs).toInt()
        mRedProgressAnimator = ValueAnimator.ofInt(getPlayingProgressPosition(), end - w2)
            .setDuration(mRightProgressPos - scrollPos - (mRedProgressBarPos - scrollPos))
        mRedProgressAnimator?.interpolator = LinearInterpolator()
        mRedProgressAnimator?.addUpdateListener { animation: ValueAnimator ->
            setPlayProgressPosition(animation.animatedValue as Int)
        }
        mRedProgressAnimator?.start()
    }

    private fun setPlayProgressPosition(x: Int) {
        val params = mRedProgressIcon.layoutParams as FrameLayout.LayoutParams
        params.leftMargin = x
        mRedProgressIcon.layoutParams = params
    }

    private fun pauseRedProgressAnimation() {
        mRedProgressIcon.clearAnimation()
        if (mRedProgressAnimator != null && mRedProgressAnimator!!.isRunning) {
            mAnimationHandler.removeCallbacks(mAnimationRunnable)
            mRedProgressAnimator!!.cancel()
        }
    }

    private val mAnimationRunnable = Runnable { updateVideoProgress() }

    private fun updateVideoProgress() {
        val currentPosition = mVideoView.currentPosition.toLong()
        if (currentPosition >= mRightProgressPos) {
            mRedProgressBarPos = mLeftProgressPos
            pauseRedProgressAnimation()
            onVideoPause()
            setPlayProgressPosition(getPlayingProgressPosition())
        } else {
            mAnimationHandler.post(mAnimationRunnable)
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
        private const val COMPRESSED_VIDEO_FILE_NAME = "compress.mp4"
    }
}