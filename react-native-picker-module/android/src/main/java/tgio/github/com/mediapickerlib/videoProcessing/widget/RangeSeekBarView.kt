package tgio.github.com.mediapickerlib.videoProcessing.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import tgio.github.com.mediapickerlib.DEBUG_TOUCH_AREA_ENABLED
import tgio.github.com.mediapickerlib.DEFAULT_MAX_DURATION
import tgio.github.com.mediapickerlib.DEFAULT_MIN_DURATION
import tgio.github.com.mediapickerlib.R
import tgio.github.com.mediapickerlib.videoProcessing.Utils
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@SuppressLint("ViewConstructor")
class RangeSeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mActivePointerId = INVALID_POINTER_ID
    private var mMinShootTime = DEFAULT_MIN_DURATION
    private var mMaxShootTime = DEFAULT_MAX_DURATION
    private var absoluteMinValuePrim = 0.0
    private var absoluteMaxValuePrim = 0.0
    private var normalizedMinValue = 0.0
    private var normalizedMaxValue = 1.0
    private var normalizedMinValueTime = 0.0
    private var normalizedMaxValueTime = 1.0
    private var mScaledTouchSlop = 0
    private var progressThumb: Bitmap
    private var thumbImageLeft: Bitmap
    private var thumbImageRight: Bitmap
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mVideoTrimTimePaintL = Paint()
    private val mVideoTrimTimePaintR = Paint()
    private val mShadow = Paint()
    private val mTouchLeft = Paint()
    private val mTouchRight = Paint()
    private var thumbWidth = 0
    private var thumbHalfWidth = 0f
    private val padding = 0f
    private var mStartPosition: Long = 0
    private var mEndPosition: Long = 0
    private var mDownMotionX = 0f
    private var mIsDragging = false
    private var pressedThumb: Thumb? = null
    private var minWidth = 1.0
    private var min = 0.0
    private var screenWidth = 0
    private var progressWidth = 0
    private var extraTouchArea = 0
    private var timeTextPadding = 0
    private var textPositionY = 0

    private var mPaddingLeft = 0
    private var mPaddingRight = 0
    private var thumbCornerRadius = 0

    private var leftThumbsTime = Utils.convertSecondsToTime(0)
    private var rightThumbsTime = Utils.convertSecondsToTime(0)
    private var leftPos = 0F
    private var progressPos = 0F
    private var leftTextPos = 0F
    private var rightPos = 0F
    private var rightTextPos = 0F

    var isNotifyWhileDragging = false
    private var mRangeSeekBarChangeListener: OnRangeSeekBarChangeListener? = null

    private val valueLength: Int
        get() = getSafeWidth() - mPaddingLeft - mPaddingRight

    var selectedMinValue: Long
        get() = normalizedToValue(normalizedMinValueTime)
        set(value) {
            if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
                setNormalizedMinValue(0.0)
            } else {
                setNormalizedMinValue(valueToNormalized(value))
            }
        }

    var selectedMaxValue: Long
        get() {
            val m = normalizedToValue(normalizedMaxValueTime)
            return m
        }
        set(value) {
            if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
                setNormalizedMaxValue(1.0)
            } else {
                setNormalizedMaxValue(valueToNormalized(value))
            }
        }


    enum class Thumb {
        L, R
    }

    fun reset() {
        normalizedMinValue = 0.0
        normalizedMaxValue = 1.0
        normalizedMinValueTime = 0.0
        normalizedMaxValueTime = 1.0

        setLeftPos(0F)
        setRightPos(screenWidth.toFloat())
        setProgressPos(0F)

        extraMsFromTimeline = 0

        selectedMinValue = 0
        selectedMaxValue = mMaxShootTime
        setStartEndTime(0, mMaxShootTime)
        calcDrawPositions()

        leftThumbsTime = Utils.convertSecondsToTime(mStartPosition / 1000)
        rightThumbsTime = Utils.convertSecondsToTime(mEndPosition / 1000)
        invalidate()
    }

    fun setParams(
        startPosition: Long,
        endPosition: Long,
        minDuration: Long,
        maxDuration: Long
    ) {
        absoluteMinValuePrim = startPosition.toDouble()
        absoluteMaxValuePrim = endPosition.toDouble()
        mMinShootTime = minDuration
        mMaxShootTime = maxDuration

        min = (mMinShootTime / (absoluteMaxValuePrim - absoluteMinValuePrim)) * (width - mPaddingLeft - mPaddingRight - thumbWidth * 2)
        minWidth = if (absoluteMaxValuePrim > 5 * 60 * 1000) {
            val df = DecimalFormat("0.0000")
            df.format(min).toDouble()
        } else {
            round(min + 0.5)
        }
    }

    init {
        screenWidth = context.resources.displayMetrics.widthPixels
        progressWidth =
            context.resources.getDimensionPixelOffset(R.dimen.bloom_native_progress_thumb_width)
        extraTouchArea =
            context.resources.getDimensionPixelOffset(R.dimen.bloom_native_extraTouchArea)
        timeTextPadding =
            context.resources.getDimensionPixelOffset(R.dimen.bloom_native_timeTextPadding)
        textPositionY =
            context.resources.getDimensionPixelOffset(R.dimen.bloom_native_rangeBarTextPaddingTop)

        setPadding(
            0,//context.resources.getDimensionPixelOffset(R.dimen.paddingTimeline),
            context.resources.getDimensionPixelOffset(R.dimen.bloom_native_rangeBarPaddingTop),
            0,//context.resources.getDimensionPixelOffset(R.dimen.paddingTimeline),
            0
        )

        thumbCornerRadius =
            context.resources.getDimensionPixelOffset(R.dimen.bloom_native_progress_thumb_corner_radius)
        mPaddingLeft =
            context.resources.getDimensionPixelOffset(R.dimen.bloom_native_paddingTimeline)
        mPaddingRight =
            context.resources.getDimensionPixelOffset(R.dimen.bloom_native_paddingTimeline)

        isFocusable = true
        isFocusableInTouchMode = true
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        thumbImageLeft = drawableToBitmap(
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.bloom_native_thumb_l,
                context.theme
            )
        )!!
        thumbImageRight = drawableToBitmap(
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.bloom_native_thumb_r,
                context.theme
            )
        )!!
        progressThumb = drawableToBitmap(
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.bloom_native_thumb_progress,
                context.theme
            )
        )!!
        thumbWidth = thumbImageLeft.width


        thumbHalfWidth = thumbWidth / 2.toFloat()
        val shadowColor = ContextCompat.getColor(context, R.color.bloom_native_shadow_color)
        mShadow.isAntiAlias = true
        mShadow.color = shadowColor

        mTouchLeft.isAntiAlias = true
        mTouchLeft.color = Color.GREEN
        mTouchLeft.alpha = 100

        mTouchRight.isAntiAlias = true
        mTouchRight.color = Color.RED
        mTouchRight.alpha = 100

        mVideoTrimTimePaintL.strokeWidth = 3f
        mVideoTrimTimePaintL.textSize = 13 * resources.displayMetrics.scaledDensity
        mVideoTrimTimePaintL.isAntiAlias = true
        mVideoTrimTimePaintL.color = ContextCompat.getColor(context, R.color.bloom_native_dark)
        mVideoTrimTimePaintL.typeface =
            ResourcesCompat.getFont(getContext(), R.font.proxima_nova_regular)
        mVideoTrimTimePaintL.textAlign = Paint.Align.LEFT
        mVideoTrimTimePaintR.strokeWidth = 3f
        mVideoTrimTimePaintR.color = ContextCompat.getColor(context, R.color.bloom_native_dark)
        mVideoTrimTimePaintR.textSize = 13 * resources.displayMetrics.scaledDensity
        mVideoTrimTimePaintR.isAntiAlias = true
        mVideoTrimTimePaintR.typeface =
            ResourcesCompat.getFont(getContext(), R.font.proxima_nova_regular)
        mVideoTrimTimePaintR.textAlign = Paint.Align.RIGHT
        calcDrawPositions()
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: OutOfMemoryError) { // Handle the error
            null
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width > 0) {
            drawShadows(canvas)
            drawVideoTrimTimeText(canvas)

            drawTouchAreas(canvas)


            drawThumb(leftPos, canvas, true)
            drawProgress(canvas)
            drawThumb(rightPos, canvas, false)

            drawMarkers(canvas)
        }
    }

    private val markerPaint = Paint().apply {
        color = Color.RED
        strokeWidth = progressWidth.toFloat()
        alpha = 100
    }

    private fun drawMarkers(canvas: Canvas) {
        if(DEBUG_TOUCH_AREA_ENABLED.not()) {
            return
        }
        val startX = mPaddingLeft.toFloat() + thumbWidth + (progressWidth / 2)
        val endX = width - mPaddingRight.toFloat() - thumbWidth - (progressWidth / 2)
        val lengthX = endX - startX
        canvas.drawLine(
            startX,
            paddingTop.toFloat(),
            startX,
            height.toFloat(),
            markerPaint
        )
        canvas.drawLine(
            (lengthX / 3F) + startX,
            paddingTop.toFloat(),
            (lengthX / 3F) + startX,
            height.toFloat(),
            markerPaint
        )
        canvas.drawLine(
            ((lengthX / 3F) * 2) + startX,
            paddingTop.toFloat(),
            ((lengthX / 3F) * 2) + startX,
            height.toFloat(),
            markerPaint
        )
        canvas.drawLine(
            endX,
            paddingTop.toFloat(),
            endX,
            height.toFloat(),
            markerPaint
        )
    }

    private fun drawTouchAreas(canvas: Canvas) {
        if (DEBUG_TOUCH_AREA_ENABLED.not()) {
            return
        }
        canvas.drawRect(
            Rect(
                leftPos.toInt() - extraTouchArea,
                paddingTop,
                leftPos.toInt() + thumbWidth + extraTouchArea,
                height
            ),
            mTouchLeft
        )
        canvas.drawRect(
            Rect(
                rightPos.toInt() - extraTouchArea,
                paddingTop,
                rightPos.toInt() + thumbWidth + extraTouchArea,
                height
            ),
            mTouchRight
        )
    }

    private fun drawShadows(canvas: Canvas) {
        canvas.drawRect(
            Rect(
                0,
                paddingTop,
                (leftPos + thumbCornerRadius + 3).toInt(),
                height
            ),
            mShadow
        )
        canvas.drawRect(
            Rect(
                rightPos.toInt() + thumbWidth - thumbCornerRadius - 3,
                paddingTop,
                getSafeWidth(),
                height
            ),
            mShadow
        )
    }

    private fun drawThumb(
        screenCoord: Float,
        canvas: Canvas,
        isLeft: Boolean
    ) {
        canvas.drawBitmap(
            if (isLeft) thumbImageLeft else thumbImageRight,
            screenCoord,
            paddingTop.toFloat(),
            paint
        )
    }

    private fun drawProgress(
        canvas: Canvas
    ) {
        canvas.drawBitmap(
            progressThumb,
            progressPos,
            paddingTop.toFloat(),
            paint
        )
    }

    fun getProgressPos() = progressPos
    fun getRightPos() = rightPos
    fun getLeftPos() = leftPos

    fun setProgressPos(value: Float) {
        val mostLeft = leftPos + thumbWidth
        val mostRight = rightPos - progressWidth
        progressPos = max(mostLeft, min(value, mostRight))
        invalidate()
    }

    private fun setLeftPos(value: Float) {
        val mostLeft = mPaddingLeft.toFloat()
        val mostRight = rightPos - progressWidth - thumbWidth - minWidth
        leftPos = max(mostLeft, min(value, mostRight.toFloat()))
    }

    private fun setRightPos(value: Float) {
        val mostLeft = leftPos + progressWidth + thumbWidth + minWidth
        val mostRight = width - thumbWidth - mPaddingRight
        rightPos = min(mostRight.toFloat(), max(mostLeft.toFloat(), value))
    }

    private fun calcDrawPositions() {
        if (pressedThumb == Thumb.L) {
            setLeftPos(normalizedToScreen(normalizedMinValue))
        } else if (pressedThumb == Thumb.R) {
            setRightPos(normalizedToScreen(normalizedMaxValue) - thumbWidth - mPaddingLeft)
        } else if (mIsSeeking.not()) {
            setLeftPos(normalizedToScreen(normalizedMinValue))
            setRightPos(normalizedToScreen(normalizedMaxValue) - thumbWidth - mPaddingLeft)
        }

        setProgressPos(leftPos)

        leftTextPos = leftPos
        rightTextPos = rightPos
        calcTextPositions()
    }

    private fun calcTextPositions() {
        val leftBounds = Rect()
        mVideoTrimTimePaintL.getTextBounds(leftThumbsTime, 0, leftThumbsTime.length, leftBounds)
        val leftWidth = leftBounds.width()
        val leftX = leftTextPos + leftWidth + timeTextPadding

        val rightBounds = Rect()
        mVideoTrimTimePaintL.getTextBounds(rightThumbsTime, 0, rightThumbsTime.length, rightBounds)
        val rightWidth = leftBounds.width()
        val rightX = rightTextPos - rightWidth - timeTextPadding


        val diff = abs(leftX - rightX)

        if (leftX >= rightX) {
            if (rightTextPos + thumbWidth + mPaddingRight >= getSafeWidth().toFloat()) {
                leftTextPos -= diff
            } else if (leftTextPos <= mPaddingLeft) {
                rightTextPos += diff
            } else {
                leftTextPos -= diff / 2
                rightTextPos += diff / 2
            }
        }
    }

    private fun drawVideoTrimTimeText(canvas: Canvas) {
        canvas.drawText(
            leftThumbsTime,
            max(mPaddingRight.toFloat(), leftTextPos),
            textPositionY.toFloat(),
            mVideoTrimTimePaintL
        )
        canvas.drawText(
            rightThumbsTime,
            min(width - mPaddingRight.toFloat(), rightTextPos + thumbWidth),
            textPositionY.toFloat(),
            mVideoTrimTimePaintR
        )
    }

    private var xDiff = 0F

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1) {
            return super.onTouchEvent(event)
        }
        if (!isEnabled) return false
        if (absoluteMaxValuePrim <= mMinShootTime || event.pointerCount > 1) {
            return super.onTouchEvent(event)
        }
        val pointerIndex: Int
        val action = event.action


        val eventX = event.getX(0)

        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = event.getPointerId(event.pointerCount - 1)
                pointerIndex = event.findPointerIndex(mActivePointerId)
                mDownMotionX = event.getX(pointerIndex)
                pressedThumb = evalPressedThumb(mDownMotionX)
                if (pressedThumb == null) return super.onTouchEvent(event)
                isPressed = true

                if (pressedThumb == Thumb.L) {
                    xDiff = mDownMotionX - leftPos
                } else if (pressedThumb == Thumb.R) {
                    xDiff = mDownMotionX - rightPos
                }

                mIsDragging = true

                attemptClaimDrag()

                if (mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener!!.onRangeSeekBarValuesChanged(
                        this,
                        selectedMinValue,
                        selectedMaxValue,
                        MotionEvent.ACTION_DOWN,
                        pressedThumb
                    )
                }
            }
            MotionEvent.ACTION_MOVE -> if (pressedThumb != null) {
                if (Thumb.L == pressedThumb) {
                    moveThumbL(eventX, xDiff)
                } else if (Thumb.R == pressedThumb) {
                    moveThumbR(eventX, xDiff)
                }

                setStartEndTime(
                    selectedMinValue + extraMsFromTimeline,
                    selectedMaxValue + extraMsFromTimeline
                )

                if (isNotifyWhileDragging) {
                    mRangeSeekBarChangeListener?.onRangeSeekBarValuesChanged(
                        bar = this,
                        minValue = selectedMinValue,
                        maxValue = selectedMaxValue,
                        action = MotionEvent.ACTION_MOVE,
                        pressedThumb = pressedThumb
                    )
                }
            }
            MotionEvent.ACTION_UP -> {
                xDiff = 0F
                pressedThumb = null
                mIsDragging = false
                isPressed = false

                setStartEndTime(
                    selectedMinValue + extraMsFromTimeline,
                    selectedMaxValue + extraMsFromTimeline
                )
                invalidate()
                mRangeSeekBarChangeListener?.onRangeSeekBarValuesChanged(
                    bar = this,
                    minValue = selectedMinValue,
                    maxValue = selectedMaxValue,
                    action = MotionEvent.ACTION_UP,
                    pressedThumb = pressedThumb
                )
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.pointerCount - 1
                mDownMotionX = event.getX(index)
                mActivePointerId = event.getPointerId(index)
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsDragging) {
                    mIsDragging = false
                    isPressed = false
                }
                invalidate()
            }
            else -> {
            }
        }
        return true
    }

    fun isRightThumbPressed(): Boolean {
        return (pressedThumb == Thumb.R)
    }

    fun alignProgressPosition() {
        if (isRightThumbPressed()) {
            setProgressPos(rightPos)
        } else {
            setProgressPos(0F)
        }
        invalidate()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and ACTION_POINTER_INDEX_MASK shr ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mDownMotionX = ev.getX(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    private fun moveThumbL(screenCoordX: Float, mod: Float) {
        leftThumbsTime = Utils.convertSecondsToTime(mStartPosition / 1000)
        setNormalizedMinValue(screenToNormalizedL(screenCoordX - mod - mPaddingLeft))
    }

    private fun moveThumbR(screenCoordX: Float, mod: Float) {
        rightThumbsTime = Utils.convertSecondsToTime(mEndPosition / 1000)
        setNormalizedMaxValue(screenToNormalizedR(screenCoordX - mod + mPaddingRight))
    }

    private fun screenToNormalizedL(screenCoord: Float): Double {
        val calcPos = (screenCoord.toDouble()) / (getSafeWidth() - thumbWidth)
        val result = min(1.0, max(0.0, calcPos))

        val total = width - mPaddingRight - thumbWidth - thumbWidth - mPaddingLeft - progressWidth
        val part = screenCoord.toDouble() - mPaddingLeft
        val calcTime = part / total
        normalizedMinValueTime = min(1.0, max(0.0, calcTime))
        return result
    }

    private fun screenToNormalizedR(screenCoord: Float): Double {
        val calcPos = screenCoord.toDouble() / getSafeWidth()
        val result = min(1.0, max(0.0, calcPos))

        val total = width - mPaddingLeft - mPaddingRight - thumbWidth * 2 - progressWidth
        val part = screenCoord.toDouble() - thumbWidth - mPaddingLeft
        val calcTime = part / total
        normalizedMaxValueTime = min(1.0, max(0.0, calcTime))
        return result
    }

    private fun getSafeWidth(): Int {
        return if (width == 0) {
            screenWidth - (mPaddingLeft + mPaddingRight)
        } else {
            width
        }
    }

    private fun evalPressedThumb(touchX: Float): Thumb? {
        var result: Thumb? = null
        val minThumbPressed = isInThumbleftPos(touchX)
        val maxThumbPressed = isInThumbrightPos(touchX)
        if (minThumbPressed && maxThumbPressed) {
            val l = abs(leftPos - touchX)
            val r = abs(rightPos + thumbHalfWidth - touchX)
            result = if (l < r) {
                Thumb.L
            } else {
                Thumb.R
            }
        } else if (minThumbPressed) {
            result =
                Thumb.L
        } else if (maxThumbPressed) {
            result =
                Thumb.R
        }
        return result
    }

    private fun isInThumbrightPos(
        touchX: Float
    ): Boolean {
        val pos = rightPos
        val min = pos - extraTouchArea
        val max = pos + extraTouchArea + thumbWidth
        return touchX > min && touchX < max
    }

    private fun isInThumbleftPos(
        touchX: Float
    ): Boolean {
        val pos = leftPos
        val min = pos - extraTouchArea
        val max = pos + extraTouchArea + thumbWidth
        return touchX > min && touchX < max
    }

    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }


    private fun normalizedToScreen(normalizedCoord: Double): Float {
        return (mPaddingLeft + normalizedCoord * getSafeWidth()).toFloat()
    }

    private fun valueToNormalized(value: Long): Double {
        return if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            0.0
        } else (value - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim)
    }

    private fun setStartEndTime(start: Long, end: Long) {
        mStartPosition = if(start > end - mMinShootTime) {
            end - mMinShootTime
        } else {
            start
        }
        mEndPosition = if(end < start + mMinShootTime) {
            start + mMinShootTime
        } else {
            end
        }
        calcDrawPositions()
    }

    fun getStartPosition() = mStartPosition
    fun getEndPosition() = mEndPosition

    private fun setNormalizedMinValue(value: Double) {
        normalizedMinValue = max(
            0.0,
            min(1.0, min(value, normalizedMaxValue))
        )
        invalidate()
    }

    private fun setNormalizedMaxValue(value: Double) {
        normalizedMaxValue = max(
            0.0,
            min(1.0, max(value, normalizedMinValue))
        )
        invalidate()
    }


    private fun normalizedToValue(normalized: Double): Long {
        return (
                absoluteMinValuePrim + normalized
                        * (absoluteMaxValuePrim - absoluteMinValuePrim)
                ).toLong()
    }

//    override fun onSaveInstanceState(): Parcelable {
//        val bundle = Bundle()
//        bundle.putParcelable("SUPER", super.onSaveInstanceState())
//        bundle.putDouble("MIN", normalizedMinValue)
//        bundle.putDouble("MAX", normalizedMaxValue)
//        bundle.putDouble("MIN_TIME", normalizedMinValueTime)
//        bundle.putDouble("MAX_TIME", normalizedMaxValueTime)
//        return bundle
//    }
//
//    override fun onRestoreInstanceState(parcel: Parcelable) {
//        val bundle = parcel as Bundle
//        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
//        normalizedMinValue = bundle.getDouble("MIN")
//        normalizedMaxValue = bundle.getDouble("MAX")
//        normalizedMinValueTime = bundle.getDouble("MIN_TIME")
//        normalizedMaxValueTime = bundle.getDouble("MAX_TIME")
//    }

    fun setOnRangeSeekBarChangeListener(listener: OnRangeSeekBarChangeListener?) {
        mRangeSeekBarChangeListener = listener
    }

    private var mDuration = 0L
    fun setDuration(duration: Long) {
        mDuration = duration
    }

    private var extraMsFromTimeline = 0L
    fun setExtraMsFromTimeline(extraMs: Long) {
        extraMsFromTimeline = extraMs
        setStartEndTime(
            selectedMinValue + extraMsFromTimeline,
            selectedMaxValue + extraMsFromTimeline
        )
        leftThumbsTime = Utils.convertSecondsToTime(mStartPosition / 1000)
        rightThumbsTime = Utils.convertSecondsToTime(mEndPosition / 1000)
        calcDrawPositions()
        invalidate()
    }

    private var mIsSeeking = false
    fun setIsSeeking(seeking: Boolean) {
        mIsSeeking = seeking
    }

    companion object {
        const val INVALID_POINTER_ID = 255
        const val ACTION_POINTER_INDEX_MASK = 0x0000ff00
        const val ACTION_POINTER_INDEX_SHIFT = 8

    }
}