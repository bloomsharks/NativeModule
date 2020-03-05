package tgio.github.com.mediapickerlib.videoProcessing.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import tgio.github.com.mediapickerlib.R

class VideoTrimmerAdapter(
    private val thumbWidth: Int
) : RecyclerView.Adapter<VideoTrimmerAdapter.TrimmerViewHolder>() {
    private val intervals: ArrayList<Long> = arrayListOf()
    private var mVideoPath: String? = null

    fun setData(
        videoLengthMs: Long,
        numThumbs: Int,
        videoPath: String
    ) {
        mVideoPath = videoPath
        val x = videoLengthMs / ( numThumbs - 1)
        for (interval in 0 until numThumbs) {
            intervals.add(x * interval)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrimmerViewHolder {
        return TrimmerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.bloom_native_video_thumb_item_layout, parent, false),
            thumbWidth
        )
    }

    override fun onBindViewHolder(holder: TrimmerViewHolder, position: Int) {
        if(mVideoPath != null) {
            val interval: Long = intervals[position] * 1000
            val options = RequestOptions().frame(interval)

            Glide.with(holder.thumbImageView.context)
                .asBitmap()
                .load("")
//                .load(mVideoPath)
                .override(holder.thumbImageView.width, holder.thumbImageView.height)
                .apply(options)
                .downsample(DownsampleStrategy.CENTER_INSIDE)
//                .placeholder(if (position % 2 == 0) R.drawable.bloom_native_thumbnail_placeholder else R.drawable.bloom_native_thumbnail_placeholder2)
                .placeholder(R.drawable.bloom_native_thumbnail_placeholder)
                .into(holder.thumbImageView)
        }
    }

    override fun getItemCount(): Int {
        return intervals.size
    }

    class TrimmerViewHolder(itemView: View, thumbWidth: Int) :
        ViewHolder(itemView) {
        var thumbImageView: ImageView = itemView.findViewById(R.id.thumb)

        init {
            val layoutParams = thumbImageView.layoutParams as LinearLayout.LayoutParams
            layoutParams.width = thumbWidth
            thumbImageView.layoutParams = layoutParams
        }
    }

}