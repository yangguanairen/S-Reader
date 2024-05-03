package com.sena.lanraragi.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.R
import com.sena.lanraragi.ui.reader.webtoon.WebtoonScaleImageView
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.ImageLoad
import com.sena.lanraragi.utils.ScaleType
import com.sena.lanraragi.utils.TouchZone


/**
 * FileName: ReaderPageAdapter
 * Author: JiaoCan
 * Date: 2024/3/28
 */

class ReaderAdapter : BaseQuickAdapter<String, ReaderAdapter.VH>() {

    private var mOnTapListener: OnTapListener? = null
    private var mOnLongPressListener: OnLongClickListener? = null

    private val scaleImageSupportList= arrayListOf(
        ".png", ".jpeg", ".jpg", ".webp"
    )
    private val photoImageSupportList = arrayListOf(
        ".gif"
    )

    private val cacheHolderList: ArrayList<VH> = arrayListOf()

    fun onScaleChange(scaleType: ScaleType) {
        cacheHolderList.forEach { it.onScaleTypeChange(scaleType) }
    }

    fun onConfigChange() {
        cacheHolderList.forEach { it.onScaleTypeChange(AppConfig.scaleMethod) }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val h = holder as VH
        cacheHolderList.add(h)
        h.onScaleTypeChange(AppConfig.scaleMethod)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as VH).onRecycle()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        cacheHolderList.remove(holder as VH)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        if (item == null) return

        when {
            scaleImageSupportList.any { item.lowercase().endsWith(it) } -> {
                holder.bindScaleView(item, position)
            }
            photoImageSupportList.any { item.lowercase().endsWith(it) } -> {
                holder.bindPhotoView(item, position)
            }
            item == "error" -> {
                holder.bindErrorView(item, context.getString(R.string.read_file_load_failed))
            }
            item.isBlank() -> {
                holder.bindEmptyView(position)
            }
            else -> {
                holder.bindErrorView(item, context.getString(R.string.read_file_is_not_support))
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(context).inflate(R.layout.item_reader, parent, false))
    }

    override fun getItemViewType(position: Int, list: List<String>): Int {
        return position
    }


    inner class VH(private val rootView: View) : RecyclerView.ViewHolder(rootView) {

        private var mainView: View? = null
        private val firstScaleType by lazy { AppConfig.scaleMethod }
        private val rootLayout: TouchToggleLayout = rootView.findViewById<TouchToggleLayout?>(R.id.rootLayout).apply {
            if (firstScaleType == ScaleType.WEBTOON) {
                enableTouch = false
                layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            }
        }
        private val errorView: TextView = rootView.findViewById(R.id.errorView)
        private val progressBar: ProgressBar = rootView.findViewById<ProgressBar?>(R.id.progressBar).apply {
            isIndeterminate = true
            max = 100
        }
        private val pageNumberView: TextView = rootView.findViewById(R.id.pageNumber)


        init {
            rootView.apply {
                setOnClickListener { mOnTapListener?.onTap(TouchZone.Center) }
                setOnLongClickListener { mOnLongPressListener?.onLongClick(this) == true }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bindPhotoView(url: String, pos: Int) {
            DebugLog.i("ReaderAdapter.VH.bindPhotoView():\n 加载资源:$url\n${firstScaleType.name}")
            pageNumberView.text = (pos + 1).toString()
            mainView = PhotoView(context).also {
                initView(it)
                ImageLoad.Builder(context)
                    .loadPic(url)
                    .doOnProgressChange { curSize, _ ->
                        progressBar.isIndeterminate = false
                        progressBar.progress = curSize
                    }
                    .doOnSuccess {
                        progressBar.visibility = GONE
                        pageNumberView.visibility = GONE
                        rootView.run {
                            setOnClickListener(null)
                            setOnLongClickListener(null)
                        }
                        updateScale(it, AppConfig.scaleMethod)
                    }
                    .doOnError {
                        bindErrorView(url, context.getString(R.string.read_file_load_failed))
                        it.visibility = GONE
                    }
                    .into(it)
                    .execute()

            }.also { setImageTapEvent(it) }
        }

        @SuppressLint("SetTextI18n")
        fun bindScaleView(url: String, pos: Int) {
            DebugLog.i("ReaderAdapter.VH.bindScaleView(): 加载资源:$url${firstScaleType.name}")
            pageNumberView.text = (pos + 1).toString()
            mainView = (if (firstScaleType == ScaleType.WEBTOON) {
                WebtoonScaleImageView(context)
            } else {
                SubsamplingScaleImageView(context)
            }).also {
                initView(it)
//                it.setMaxTileSize(getMaxTextureSize())
//                it.setMinimumTileDpi(160)

                ImageLoad.Builder(context)
                    .loadPic(url)
                    .doOnProgressChange { curSize, _ ->
                        progressBar.isIndeterminate = false
                        progressBar.progress = curSize
                    }
                    .doOnSuccess {
                        progressBar.visibility = GONE
                        pageNumberView.visibility = GONE
                        rootView.run {
                            setOnClickListener(null)
                            setOnLongClickListener(null)
                        }
                        updateScale(it, AppConfig.scaleMethod)
                    }
                    .doOnError {
                        bindErrorView(url, context.getString(R.string.read_file_load_failed))
                        it.visibility = GONE
                    }
                    .into(it)
                    .execute()
            }.also { setImageTapEvent(it) }
        }

        @SuppressLint("ClickableViewAccessibility")
        fun bindErrorView(url: String, text: String) {
            DebugLog.e("ReaderAdapter.VH.bindErrorView(): text: $text 不支持的资源:$url")
            progressBar.visibility = View.INVISIBLE
            pageNumberView.visibility = View.INVISIBLE
            errorView.apply {
                val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        mOnTapListener?.onTap(getTouchZone(e.x, rootView))
                        return true
                    }
                })
                setOnTouchListener { _, e ->
                    gestureDetector.onTouchEvent(e)
                    true
                }
                setOnLongClickListener { mOnLongPressListener?.onLongClick(this) == true }
                visibility = View.VISIBLE
                setText(text)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bindEmptyView(pos: Int) {
            pageNumberView.text = (pos + 1).toString()
            progressBar.visibility = View.VISIBLE
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setImageTapEvent(view: View) {
            if (firstScaleType != ScaleType.WEBTOON) {
                if (view is SubsamplingScaleImageView) {
                    val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            mOnTapListener?.onTap(getTouchZone(e.x, view))
                            return true
                        }
                    })
                    view.setOnTouchListener { _, e -> gestureDetector.onTouchEvent(e) }
                } else if (view is PhotoView) {
                    view.setOnViewTapListener { _, x, _ -> mOnTapListener?.onTap(getTouchZone(x, view)) }
                }
            }
            view.setOnLongClickListener { mOnLongPressListener?.onLongClick(view) == true }
        }

        private fun initView(view: View) {
            view.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                if (firstScaleType == ScaleType.WEBTOON) {
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                } else {
                    RelativeLayout.LayoutParams.MATCH_PARENT
                }
            )
            rootLayout.addView(view)
            progressBar.bringToFront()
        }

        private fun updateScale(view: View?, scaleType: ScaleType) {
            when (view) {
                is SubsamplingScaleImageView -> {
                    when (scaleType) {
                        ScaleType.FIT_PAGE -> {
                            val hPadding = view.paddingLeft - view.paddingRight
                            val viewWidth = view.width
                            val minScale = (viewWidth - hPadding) / view.sWidth.toFloat()
                            view.minScale = minScale
                            view.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM)
                            view.resetScaleAndCenter()
                        }
                        ScaleType.FIT_HEIGHT -> {
                            val vPadding = view.paddingBottom - view.paddingTop
                            val viewHeight = view.height
                            val minScale = (viewHeight - vPadding) / view.sHeight.toFloat()
                            view.minScale = minScale
                            view.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM)
                            view.setScaleAndCenter(minScale, PointF(0f, 0f))
                        }
                        ScaleType.WEBTOON, ScaleType.FIT_WIDTH -> {
                            val hPadding = view.paddingLeft - view.paddingRight
                            val viewWidth = view.width
                            val minScale = (viewWidth - hPadding) / view.sWidth.toFloat()
                            view.minScale = minScale
                            view.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM)
                            view.setScaleAndCenter(minScale, PointF(0f, 0f))
                        }
                    }
                }
                is PhotoView -> {
                    view.scaleType = when (scaleType) {
                        ScaleType.FIT_WIDTH -> ImageView.ScaleType.FIT_CENTER
                        ScaleType.FIT_HEIGHT -> ImageView.ScaleType.FIT_START
                        ScaleType.FIT_PAGE -> ImageView.ScaleType.CENTER
                        ScaleType.WEBTOON -> ImageView.ScaleType.FIT_CENTER
                    }
                }
            }
        }

        private fun getTouchZone(x: Float, view: View) : TouchZone {
            val location = x / view.width

            if (location <= 0.25)
                return TouchZone.Left

            if (location >= 0.75)
                return TouchZone.Right

            return TouchZone.Center
        }

        fun onScaleTypeChange(scaleType: ScaleType) {
            updateScale(mainView, scaleType)
        }

        fun onRecycle() {
            (mainView as? SubsamplingScaleImageView?)?.recycle()
            rootLayout.removeView(mainView)
            mainView = null
        }
    }

    fun setOnImageClickListener(func: (touchZone: TouchZone) -> Unit)  {
        mOnTapListener = object : OnTapListener {
            override fun onTap(touchZone: TouchZone) {
                func.invoke(touchZone)
            }
        }
    }

    fun setOnImageLongClickListener(func: () -> Boolean)  {
        mOnLongPressListener = OnLongClickListener { func.invoke() }
    }

    private interface OnTapListener {
        fun onTap(touchZone: TouchZone)
    }
}

