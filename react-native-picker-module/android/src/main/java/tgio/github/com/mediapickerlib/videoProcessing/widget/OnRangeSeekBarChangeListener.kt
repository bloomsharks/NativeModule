package tgio.github.com.mediapickerlib.videoProcessing.widget

interface OnRangeSeekBarChangeListener {
    fun onRangeSeekBarValuesChanged(
        bar: RangeSeekBarView?,
        minValue: Long,
        maxValue: Long,
        action: Int,
        positionHasBeenChanged: Boolean,
        pressedThumb: RangeSeekBarView.Thumb?
    )
}