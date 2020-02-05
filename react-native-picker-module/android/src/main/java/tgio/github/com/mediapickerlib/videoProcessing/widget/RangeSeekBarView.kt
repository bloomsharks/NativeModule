package tgio.github.com.mediapickerlib.videoProcessing.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
    defStyleAttr: Int = 0,
    mLeftProgressPos: Long,
    mRightProgressPos: Long
) : View(context, attrs, defStyleAttr)  {
    private var mActivePointerId = INVALID_POINTER_ID
    private var mMinShootTime = DEFAULT_MIN_DURATION
    private var absoluteMinValuePrim = 0.0
    private var absoluteMaxValuePrim = 0.0
    private var normalizedMinValue = 0.0
    private var normalizedMaxValue = 1.0
    private var normalizedMinValueTime = 0.0
    private var normalizedMaxValueTime = 1.0
    private var mScaledTouchSlop = 0
    private var thumbImageLeft: Bitmap
    private var thumbImageRight: Bitmap
    private var thumbPressedImage: Bitmap
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mLine = Paint()
    private val mVideoTrimTimePaintL = Paint()
    private val mVideoTrimTimePaintR = Paint()
    private val mShadow = Paint()
    private var thumbWidth = 0
    private var thumbHalfWidth = 0f
    private val padding = 0f
    private var mStartPosition: Long = 0
    private var mEndPosition: Long = 0
    private var isTouchDown = false
    private var mDownMotionX = 0f
    private var mIsDragging = false
    private var pressedThumb: Thumb? = null
    private var isMin = false
    private var min_width = 1.0
    private var mPaddingTop = 0
    private var textPositionY = 0
    private var linePositionY = 0
    private var lineHeight = 0
    private var lineThickness = 0

    var isNotifyWhileDragging = false
    private var mRangeSeekBarChangeListener: OnRangeSeekBarChangeListener? = null

    enum class Thumb {
        MIN, MAX
    }

    fun reset() {
        normalizedMinValue = 0.0
        normalizedMaxValue = 1.0
        normalizedMinValueTime = 0.0
        normalizedMaxValueTime = 1.0
    }

    init {
        this.absoluteMinValuePrim = mLeftProgressPos.toDouble()
        this.absoluteMaxValuePrim = mRightProgressPos.toDouble()
        mPaddingTop = context.resources.getDimensionPixelOffset(R.dimen.bloom_native_rangeBarPaddingTop)
        textPositionY = context.resources.getDimensionPixelOffset(R.dimen.bloom_native_rangeBarTextPaddingTop)
        linePositionY = context.resources.getDimensionPixelOffset(R.dimen.bloom_native_linePositionY)
        lineHeight = context.resources.getDimensionPixelOffset(R.dimen.bloom_native_lineHeight)
        lineThickness = context.resources.getDimensionPixelOffset(R.dimen.bloom_native_lineThickness)

        isFocusable = true
        isFocusableInTouchMode = true
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        thumbImageLeft = drawableToBitmap(
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.bloom_native_thumb,
                context.theme
            )
        )!!
        thumbWidth = thumbImageLeft.width
        thumbImageRight = thumbImageLeft
        thumbPressedImage = thumbImageLeft

        mLine.isAntiAlias = false
        mLine.color = Color.BLACK
        mLine.strokeWidth = lineThickness.toFloat()

        thumbHalfWidth = thumbWidth / 2.toFloat()
        val shadowColor = ContextCompat.getColor(context, R.color.bloom_native_shadow_color)
        mShadow.isAntiAlias = true
        mShadow.color = shadowColor
        mVideoTrimTimePaintL.strokeWidth = 3f
        mVideoTrimTimePaintL.textSize = 13 * resources.displayMetrics.scaledDensity
        mVideoTrimTimePaintL.isAntiAlias = true
        mVideoTrimTimePaintL.color = Color.BLACK
        mVideoTrimTimePaintL.textAlign = Paint.Align.LEFT
        mVideoTrimTimePaintR.strokeWidth = 3f
        mVideoTrimTimePaintR.color = Color.BLACK
        mVideoTrimTimePaintR.textSize = 13 * resources.displayMetrics.scaledDensity
        mVideoTrimTimePaintR.isAntiAlias = true
        mVideoTrimTimePaintR.color = Color.BLACK
        mVideoTrimTimePaintR.textAlign = Paint.Align.RIGHT
    }

    fun setMinDuration(minDuration: Long) {
        mMinShootTime = minDuration
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

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bg_middle_left = 0f
        val bg_middle_right = width - paddingRight.toFloat()
        val rangeL = normalizedToScreen(normalizedMinValue)
        val rangeR = normalizedToScreen(normalizedMaxValue)
        val leftRect = Rect(
            bg_middle_left.toInt(),
            mPaddingTop,
            rangeL.toInt(),
            height
        )
        val rightRect = Rect(
            rangeR.toInt(),
            mPaddingTop,
            bg_middle_right.toInt(),
            height
        )
        canvas.drawRect(leftRect, mShadow)
        canvas.drawRect(rightRect, mShadow)

        drawThumb(normalizedToScreen(normalizedMinValue), false, canvas, true)
        drawThumb(normalizedToScreen(normalizedMaxValue), false, canvas, false)
        drawVideoTrimTimeText(canvas)
        drawLines(canvas)
    }

    private fun drawThumb(
        screenCoord: Float,
        pressed: Boolean,
        canvas: Canvas,
        isLeft: Boolean
    ) {
        canvas.drawBitmap(
            if (pressed) {
                thumbPressedImage
            } else if (isLeft) thumbImageLeft else thumbImageRight,
            screenCoord - if (isLeft) 0 else thumbWidth,
            mPaddingTop.toFloat(),
            paint
        )
    }

    private fun drawLines(canvas: Canvas) {
        val leftPos = normalizedToScreen(normalizedMinValue)
        val rightPos = normalizedToScreen(normalizedMaxValue)

        canvas.drawLine(
            leftPos + thumbHalfWidth,
            textPositionY + linePositionY.toFloat(),
            leftPos + thumbHalfWidth,
            textPositionY + lineHeight.toFloat(),
            mLine
        )
        canvas.drawLine(
            rightPos - thumbHalfWidth,
            textPositionY + linePositionY.toFloat(),
            rightPos - thumbHalfWidth,
            textPositionY + lineHeight.toFloat(),
            mLine
        )
    }

    private fun drawVideoTrimTimeText(canvas: Canvas) {
        val leftThumbsTime = Utils.convertSecondsToTime(mStartPosition)
        val rightThumbsTime = Utils.convertSecondsToTime(mEndPosition)

        var leftPos = normalizedToScreen(normalizedMinValue)
        var rightPos = normalizedToScreen(normalizedMaxValue)


        val leftBounds = Rect()
        mVideoTrimTimePaintL.getTextBounds(leftThumbsTime, 0, leftThumbsTime.length, leftBounds)
        val leftWidth = leftBounds.width()

        val rightBounds = Rect()
        mVideoTrimTimePaintL.getTextBounds(rightThumbsTime, 0, rightThumbsTime.length, rightBounds)
        val rightWidth = leftBounds.width()


        val leftX = leftPos + leftWidth + 10
        val rightX = rightPos - rightWidth - 10


        val diff = abs(leftX - rightX)

        if (leftX >= rightX) {
            if (rightPos >= width.toFloat()) {
                leftPos -= diff
            } else {
                leftPos -= diff / 2
            }
        }

        if (rightX <= leftX) {
            if (leftPos <= 0F) {
                rightPos += diff
            } else {
                rightPos += diff / 2
            }
        }
        canvas.drawText(
            leftThumbsTime,
            max(0F, leftPos),
            textPositionY.toFloat(),
            mVideoTrimTimePaintL
        )
        canvas.drawText(
            rightThumbsTime,
            min(width.toFloat(), rightPos),
            textPositionY.toFloat(),
            mVideoTrimTimePaintR
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isTouchDown) {
            return super.onTouchEvent(event)
        }
        if (event.pointerCount > 1) {
            return super.onTouchEvent(event)
        }
        if (!isEnabled) return false
        if (absoluteMaxValuePrim <= mMinShootTime) {
            return super.onTouchEvent(event)
        }
        val pointerIndex: Int
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = event.getPointerId(event.pointerCount - 1)
                pointerIndex = event.findPointerIndex(mActivePointerId)
                mDownMotionX = event.getX(pointerIndex)
                pressedThumb = evalPressedThumb(mDownMotionX)
                if (pressedThumb == null) return super.onTouchEvent(event)
                isPressed = true
                onStartTrackingTouch()
                trackTouchEvent(event)
                attemptClaimDrag()
                if (mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener!!.onRangeSeekBarValuesChanged(
                        this,
                        selectedMinValue,
                        selectedMaxValue,
                        MotionEvent.ACTION_DOWN,
                        isMin,
                        pressedThumb
                    )
                }
            }
            MotionEvent.ACTION_MOVE -> if (pressedThumb != null) {
                if (mIsDragging) {
                    trackTouchEvent(event)
                } else {
                    pointerIndex = event.findPointerIndex(mActivePointerId)
                    val x = event.getX(pointerIndex)
                    if (abs(x - mDownMotionX) > mScaledTouchSlop) {
                        isPressed = true
                        invalidate()
                        onStartTrackingTouch()
                        trackTouchEvent(event)
                        attemptClaimDrag()
                    }
                }
                if (isNotifyWhileDragging && mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener!!.onRangeSeekBarValuesChanged(
                        this, selectedMinValue, selectedMaxValue, MotionEvent.ACTION_MOVE,
                        isMin, pressedThumb
                    )
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }
                invalidate()
                if (mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener!!.onRangeSeekBarValuesChanged(
                        this,
                        selectedMinValue,
                        selectedMaxValue,
                        MotionEvent.ACTION_UP,
                        isMin,
                        pressedThumb
                    )
                }
                pressedThumb = null
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
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate()
            }
            else -> {
            }
        }
        return true
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

    private fun trackTouchEvent(event: MotionEvent) {
        if (event.pointerCount > 1) return
        val pointerIndex = event.findPointerIndex(mActivePointerId)
        var x = 0f
        x = try {
            event.getX(pointerIndex)
        } catch (e: Exception) {
            return
        }
        if (Thumb.MIN == pressedThumb) {
            setNormalizedMinValue(screenToNormalized(x, 0))
        } else if (Thumb.MAX == pressedThumb) {
            setNormalizedMaxValue(screenToNormalized(x, 1))
        }
    }

    private fun screenToNormalized(screenCoord: Float, position: Int): Double {
        val width = width
        return if (width <= 2 * padding) {
            0.0
        } else {
            isMin = false
            var current_width = screenCoord.toDouble()
            val rangeL = normalizedToScreen(normalizedMinValue)
            val rangeR = normalizedToScreen(normalizedMaxValue)
            val min =
                mMinShootTime / (absoluteMaxValuePrim - absoluteMinValuePrim) * (width - thumbWidth * 2)
            min_width = if (absoluteMaxValuePrim > 5 * 60 * 1000) {
                val df = DecimalFormat("0.0000")
                df.format(min).toDouble()
            } else {
                round(min + 0.5)
            }
            if (position == 0) {
                if (isInThumbRangeLeft(screenCoord, normalizedMinValue, 0.5)) {
                    return normalizedMinValue
                }
                val rightPosition: Float =
                    if (getWidth() - rangeR >= 0) getWidth() - rangeR else 0F
                val left_length = valueLength - (rightPosition + min_width)
                if (current_width > rangeL) {
                    current_width = rangeL + (current_width - rangeL)
                } else if (current_width <= rangeL) {
                    current_width = rangeL - (rangeL - current_width)
                }
                if (current_width > left_length) {
                    isMin = true
                    current_width = left_length
                }
                if (current_width < thumbWidth * 2 / 3) {
                    current_width = 0.0
                }
                val resultTime = (current_width - padding) / (width - 2 * thumbWidth)
                normalizedMinValueTime =
                    min(1.0, max(0.0, resultTime))
                val result = (current_width - padding) / (width - 2 * padding)
                min(
                    1.0,
                    max(0.0, result)
                )
            } else {
                if (isInThumbRange(screenCoord, normalizedMaxValue, 0.5)) {
                    return normalizedMaxValue
                }
                val right_length = valueLength - (rangeL + min_width)
                if (current_width > rangeR) {
                    current_width = rangeR + (current_width - rangeR)
                } else if (current_width <= rangeR) {
                    current_width = rangeR - (rangeR - current_width)
                }
                var paddingRight = getWidth() - current_width
                if (paddingRight > right_length) {
                    isMin = true
                    current_width = getWidth() - right_length
                    paddingRight = right_length
                }
                if (paddingRight < thumbWidth * 2 / 3) {
                    current_width = getWidth().toDouble()
                    paddingRight = 0.0
                }
                var resultTime = (paddingRight - padding) / (width - 2 * thumbWidth)
                resultTime = 1 - resultTime
                normalizedMaxValueTime =
                    min(1.0, max(0.0, resultTime))
                val result = (current_width - padding) / (width - 2 * padding)
                min(
                    1.0,
                    max(0.0, result)
                )
            }
        }
    }

    private val valueLength: Int
        private get() = width - 2 * thumbWidth

    private fun evalPressedThumb(touchX: Float): Thumb? {
        var result: Thumb? = null
        val minThumbPressed =
            isInThumbRange(touchX, normalizedMinValue, 2.0)
        val maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue, 2.0)
        if (minThumbPressed && maxThumbPressed) {
            result = if (touchX / width > 0.5f) Thumb.MIN else Thumb.MAX
        } else if (minThumbPressed) {
            result = Thumb.MIN
        } else if (maxThumbPressed) {
            result = Thumb.MAX
        }
        return result
    }

    private fun isInThumbRange(
        touchX: Float,
        normalizedThumbValue: Double,
        scale: Double
    ): Boolean {
        return abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth * scale
    }

    private fun isInThumbRangeLeft(
        touchX: Float,
        normalizedThumbValue: Double,
        scale: Double
    ): Boolean {
        return abs(touchX - normalizedToScreen(normalizedThumbValue) - thumbWidth) <= thumbHalfWidth * scale
    }

    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    fun onStartTrackingTouch() {
        mIsDragging = true
    }

    fun onStopTrackingTouch() {
        mIsDragging = false
    }

    fun setMinShootTime(min_cut_time: Long) {
        mMinShootTime = min_cut_time
    }

    private fun normalizedToScreen(normalizedCoord: Double): Float {
        return (paddingLeft + normalizedCoord * (width - paddingLeft - paddingRight)).toFloat()
    }

    private fun valueToNormalized(value: Long): Double {
        return if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            0.0
        } else (value - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim)
    }

    fun setStartEndTime(start: Long, end: Long) {
        mStartPosition = start / 1000
        mEndPosition = end / 1000
    }

    fun getStartPosition() = mStartPosition
    fun getEndPosition() = mEndPosition

    fun setNormalizedMinValue(value: Double) {
        normalizedMinValue = max(
            0.0,
            min(1.0, min(value, normalizedMaxValue))
        )
        invalidate()
    }

    fun setNormalizedMaxValue(value: Double) {
        normalizedMaxValue = max(
            0.0,
            min(1.0, max(value, normalizedMinValue))
        )
        invalidate()
    }

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
        get() = normalizedToValue(normalizedMaxValueTime)
        set(value) {
            if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
                setNormalizedMaxValue(1.0)
            } else {
                setNormalizedMaxValue(valueToNormalized(value))
            }
        }

    private fun normalizedToValue(normalized: Double): Long {
        return (absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim)).toLong()
    }

    fun setTouchDown(touchDown: Boolean) {
        isTouchDown = touchDown
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        bundle.putDouble("MIN", normalizedMinValue)
        bundle.putDouble("MAX", normalizedMaxValue)
        bundle.putDouble("MIN_TIME", normalizedMinValueTime)
        bundle.putDouble("MAX_TIME", normalizedMaxValueTime)
        return bundle
    }

    override fun onRestoreInstanceState(parcel: Parcelable) {
        val bundle = parcel as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        normalizedMinValue = bundle.getDouble("MIN")
        normalizedMaxValue = bundle.getDouble("MAX")
        normalizedMinValueTime = bundle.getDouble("MIN_TIME")
        normalizedMaxValueTime = bundle.getDouble("MAX_TIME")
    }

    interface OnRangeSeekBarChangeListener {
        fun onRangeSeekBarValuesChanged(
            bar: RangeSeekBarView?,
            minValue: Long,
            maxValue: Long,
            action: Int,
            isMin: Boolean,
            pressedThumb: Thumb?
        )
    }

    fun setOnRangeSeekBarChangeListener(listener: OnRangeSeekBarChangeListener?) {
        mRangeSeekBarChangeListener = listener
    }

    companion object {
        const val INVALID_POINTER_ID = 255
        const val ACTION_POINTER_INDEX_MASK = 0x0000ff00
        const val ACTION_POINTER_INDEX_SHIFT = 8
    }
}