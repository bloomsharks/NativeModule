package tgio.github.com.mediapickerlib.videoProcessing.widget

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.VideoView

class ZVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
): VideoView(context, attrs, defStyleAttr) {
    private var mVideoWidth = 480
    private var mVideoHeight = 480
    private var videoRealW = 1
    private var videoRealH = 1

    override fun setVideoURI(uri: Uri) {
        super.setVideoURI(uri)
        val retr = MediaMetadataRetriever()
        retr.setDataSource(uri.path)
        val height =
            retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val width =
            retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        try {
            videoRealH = height.toInt()
            videoRealW = width.toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = View.getDefaultSize(0, widthMeasureSpec)
        val height = View.getDefaultSize(0, heightMeasureSpec)
        if (height > width) {
            if (videoRealH > videoRealW) {
                mVideoHeight = height
                mVideoWidth = width
            } else {
                mVideoWidth = width
                val r = videoRealH / videoRealW.toFloat()
                mVideoHeight = (mVideoWidth * r).toInt()
            }
        } else {
            if (videoRealH > videoRealW) {
                mVideoHeight = height
                val r = videoRealW / videoRealH.toFloat()
                mVideoWidth = (mVideoHeight * r).toInt()
            } else {
                mVideoHeight = height
                mVideoWidth = width
            }
        }
        if (videoRealH == videoRealW && videoRealH == 1) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            setMeasuredDimension(mVideoWidth, mVideoHeight)
        }
    }
}