package tgio.github.com.mediapickerlib.videoProcessing.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tgio.github.com.mediapickerlib.R

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr)  {

    private val layoutManager: LinearLayoutManager
    private val recyclerView: RecyclerView
    private lateinit var adapter: VideoTrimmerAdapter

    private var screenWidth = 0
    private var horizontalPadding = 0
    private var paddingTimeline = 0
    private var videoFramesWidth = 0
    private var dataIsSet = false

    init {
        screenWidth = resources.displayMetrics.widthPixels
        paddingTimeline = resources.getDimensionPixelOffset(R.dimen.bloom_native_paddingTimeline)
        horizontalPadding = paddingTimeline + resources.getDimensionPixelOffset(R.dimen.bloom_native_thumb_width)
        videoFramesWidth = screenWidth - horizontalPadding * 2

        layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        setBackgroundColor(Color.BLACK)
        recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        addView(recyclerView)
    }

    fun setData(
        videoLengthMs: Long,
        numThumbs: Int,
        videoPath: String,
        maxDisplayedThumbsCount: Int
    ) {
        if(dataIsSet)
            return
        recyclerView.addItemDecoration(
            SpacesItemDecoration2(
                horizontalPadding,
                numThumbs
            )
        )
        adapter =
            VideoTrimmerAdapter(
                videoFramesWidth / maxDisplayedThumbsCount
            )
        adapter.setData(videoLengthMs, numThumbs, videoPath)
        recyclerView.adapter = adapter
        dataIsSet = true
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun reset() {
        recyclerView.scrollToPosition(0)
    }

    fun addOnScrollListener(listener: RecyclerView.OnScrollListener) {
        recyclerView.addOnScrollListener(listener)
    }

    fun calcScrollXDistance(): Int {
        val position = layoutManager.findFirstVisibleItemPosition()
        val firstVisibleChildView = layoutManager.findViewByPosition(position)
        val itemWidth = firstVisibleChildView!!.width
        return position * itemWidth - firstVisibleChildView.left
    }

}