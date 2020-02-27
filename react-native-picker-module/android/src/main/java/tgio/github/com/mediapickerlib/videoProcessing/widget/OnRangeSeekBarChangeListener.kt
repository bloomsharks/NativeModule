package tgio.github.com.mediapickerlib.videoProcessing.widget

interface OnRangeSeekBarChangeListener {
    fun onRangeSeekBarValuesChanged(
        bar: RangeSeekBarView?,
        minValue: Long,
        maxValue: Long,
        action: Int,
        pressedThumb: RangeSeekBarView.Thumb?
    )
}