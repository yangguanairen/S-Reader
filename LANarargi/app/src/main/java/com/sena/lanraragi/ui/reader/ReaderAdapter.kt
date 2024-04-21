package com.sena.lanraragi.ui.reader

import android.content.Context
import android.view.GestureDetector.OnDoubleTapListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseDifferAdapter
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.sena.lanraragi.R
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.ImageLoad
import com.sena.lanraragi.utils.ScaleType


/**
 * FileName: ReaderPageAdapter
 * Author: JiaoCan
 * Date: 2024/3/28
 */

class ReaderAdapter : BaseDifferAdapter<Pair<String, ScaleType>, ReaderAdapter.VH>(DiffCallback()) {

    private  var mOnClickListener: OnClickListener? = null
    private var mOnLongClickListener: OnLongClickListener? = null

    private val scaleImageSupportList= arrayListOf(
        ".png", ".jpeg", ".jpg", ".webp"
    )
    private val photoImageSupportList = arrayListOf(
        ".gif"
    )

    override fun onBindViewHolder(holder: VH, position: Int, item: Pair<String, ScaleType>?) {
        if (item == null) return
        val url = item.first
        val scaleType = item.second
        
        when {
            scaleImageSupportList.any { url.lowercase().endsWith(it) } -> {
                holder.bindScaleView(url, scaleType, position)
            }
            photoImageSupportList.any { url.lowercase().endsWith(it) } -> {
                holder.bindPhotoView(url, scaleType, position)
            }
            url.isBlank() -> {
                holder.bindEmptyView(position)
            }
            else -> {
                holder.bindErrorView(url, scaleType, position)
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        val firstItemScaleType = items.getOrNull(0)?.second
        return if (firstItemScaleType == ScaleType.WEBTOON) {
            VH(LayoutInflater.from(context).inflate(R.layout.item_reader_webtoon, parent, false))
        } else {
            VH(LayoutInflater.from(context).inflate(R.layout.item_reader, parent, false))
        }
    }

    override fun getItemViewType(position: Int, list: List<Pair<String, ScaleType>>): Int {
        return position
    }


    inner class VH(rootView: View) : RecyclerView.ViewHolder(rootView) {

        private val imageLayout: RelativeLayout = rootView.findViewById(R.id.imageLayout)
        private val photoView: PhotoView = rootView.findViewById(R.id.photoView)
        private val scaleView: SubsamplingScaleImageView = rootView.findViewById(R.id.scaleView)
        private val errorView: TextView = rootView.findViewById(R.id.errorView)
        private val progressBar: ProgressBar = rootView.findViewById(R.id.progressBar)
        
        fun bindPhotoView(url: String, type: ScaleType, pos: Int) {
            DebugLog.i("ReaderAdapter.VH.bindPhotoView():\n 加载资源:$url\n scaleType:${type.name}")
            val realScaleType = when (type) {
                ScaleType.FIT_WIDTH -> ImageView.ScaleType.FIT_CENTER
                ScaleType.FIT_HEIGHT -> ImageView.ScaleType.FIT_START
                ScaleType.FIT_PAGE -> ImageView.ScaleType.CENTER
                ScaleType.WEBTOON -> ImageView.ScaleType.FIT_CENTER
            }
            photoView.apply {
                scaleType = realScaleType
                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@ReaderAdapter, it, pos)
                    true
                }
                if (type == ScaleType.WEBTOON) {
                    setOnDoubleTapListener(object : OnDoubleTapListener {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            mOnClickListener?.onClick(this@ReaderAdapter, this@apply, pos)
                            return true
                        }

                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            mOnClickListener?.onClick(this@ReaderAdapter, this@apply, pos)
                            return true
                        }

                        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                            return true
                        }

                    })
                    setOnScaleChangeListener(null)
                }
            }
            ImageLoad.Builder(context)
                .loadPic(url)
                .doOnStart {
                    photoView.visibility = View.VISIBLE
                }
                .doOnSuccess {
                    progressBar.visibility = View.INVISIBLE
                    errorView.visibility = View.INVISIBLE
                    photoView.visibility = View.VISIBLE
                    scaleView.visibility = View.INVISIBLE
                }
                .doOnError {
                    bindErrorView(url, type, pos)
                }
                .into(photoView)
                .execute()
        }

        fun bindScaleView(url: String, scaleType: ScaleType, pos: Int) {
            DebugLog.i("ReaderAdapter.VH.bindScaleView(): 加载资源:$url, scaleType:${scaleType.name}")
            val realScaleType = when (scaleType) {
                ScaleType.FIT_WIDTH -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                ScaleType.FIT_HEIGHT -> SubsamplingScaleImageView.SCALE_TYPE_START
                ScaleType.FIT_PAGE -> SubsamplingScaleImageView.SCALE_TYPE_CUSTOM
                ScaleType.WEBTOON -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
            }
//
//            scaleView.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
//                override fun onReady() {
//                    super.onReady()
//                    DebugLog.e("测试: onReady")
//                    val fadeIn = AlphaAnimation(1f, 0f).apply { duration = 300L }
//                    fadeIn.setAnimationListener(object : Animation.AnimationListener {
//                        override fun onAnimationStart(animation: Animation?) {}
//                        override fun onAnimationEnd(animation: Animation?) {
//                            DebugLog.e("测试: onAnimationEnd")
//
//
//
//                        }
//                        override fun onAnimationRepeat(animation: Animation?) {}
//                    })
//                    progressBar.startAnimation(fadeIn)
//                }
//                override fun onImageLoaded() {
//                    DebugLog.e("测试: onImageLoaded")
//                }
//            })
            scaleView.apply {
                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@ReaderAdapter, it, pos)
                    true
                }
                setMinimumScaleType(realScaleType)
            }

            ImageLoad.Builder(context)
                .loadPic(url)
                .doOnStart {
                    scaleView.visibility = View.VISIBLE
                }
                .doOnSuccess {
                    progressBar.visibility = View.INVISIBLE
                    errorView.visibility = View.INVISIBLE
                    photoView.visibility = View.INVISIBLE
                    scaleView.visibility = View.VISIBLE
                }
                .doOnError {
                    bindErrorView(url, scaleType, pos)
                }
                .into(scaleView)
                .execute()
        }

        fun bindErrorView(url: String, scaleType: ScaleType, pos: Int) {
            DebugLog.e("ReaderAdapter.VH.bindScaleView(): 不支持的资源:$url, scaleType:${scaleType.name}")
            errorView.apply {
                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@ReaderAdapter, it, pos)
                    true
                }
            }

            progressBar.visibility = View.INVISIBLE
            errorView.visibility = View.VISIBLE
            photoView.visibility = View.INVISIBLE
            scaleView.visibility = View.INVISIBLE
        }

        fun bindEmptyView(pos: Int) {
            imageLayout.apply {
                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@ReaderAdapter, it, pos)
                    true
                }
            }

            progressBar.visibility = View.VISIBLE
            errorView.visibility = View.INVISIBLE
            photoView.visibility = View.INVISIBLE
            scaleView.visibility = View.INVISIBLE
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Pair<String, ScaleType>>() {
        override fun areItemsTheSame(oldItem: Pair<String, ScaleType>, newItem: Pair<String, ScaleType>): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(oldItem: Pair<String, ScaleType>, newItem: Pair<String, ScaleType>): Boolean {
            return oldItem.second == newItem.second
        }

    }
    fun setOnImageClickListener(func: (a: ReaderAdapter, v: View, p: Int) -> Unit)  {
        mOnClickListener = object : OnClickListener {
            override fun onClick(a: ReaderAdapter, v: View, p: Int) {
                func.invoke(a, v, p)
            }
        }
    }

    fun setOnImageLongClickListener(func: (a: ReaderAdapter, v: View, p: Int) -> Boolean)  {
        mOnLongClickListener = object : OnLongClickListener {
            override fun onLongClick(a: ReaderAdapter, v: View, p: Int): Boolean {
                return func.invoke(a, v, p)
            }
        }
    }

    private interface OnClickListener {
        fun onClick(a: ReaderAdapter, v: View, p: Int)
    }

    private interface OnLongClickListener {
        fun onLongClick(a: ReaderAdapter, v: View, p: Int): Boolean
    }

}

