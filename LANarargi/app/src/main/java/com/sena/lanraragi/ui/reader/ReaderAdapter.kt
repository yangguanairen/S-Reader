package com.sena.lanraragi.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.size.ViewSizeResolver
import com.chad.library.adapter4.BaseDifferAdapter
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.sena.lanraragi.R
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.ImageLoad
import com.sena.lanraragi.utils.NewHttpHelper
import com.sena.lanraragi.utils.ScaleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception


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

    private val viewList: ArrayList<VH> = arrayListOf()

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        DebugLog.e("测试: onAttach")
        viewList.add(holder as VH)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        DebugLog.e("测试: onRecycled")
        viewList.remove(holder as VH)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        DebugLog.e("测试: onDetach")

    }


    fun onScaleTest(scaleType: ScaleType) {
        viewList.forEach {
            it.onScaleTypeChange(scaleType)
        }
    }


    override fun onBindViewHolder(holder: VH, position: Int, item: Pair<String, ScaleType>?) {
        if (item == null) return
        val url = item.first
        val scaleType = item.second
        // 自适应宽高且不响应触摸事件
        if (scaleType == ScaleType.WEBTOON) {
            holder.rootLayout.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            holder.rootLayout.enableTouch = false
        }
        holder.rootLayout.apply {
            setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, this, position) }
            setOnLongClickListener { mOnLongClickListener?.onLongClick(this@ReaderAdapter, this, position) == true }
        }


        CoroutineScope(Dispatchers.Main).launch {
            val id = Regex("api/archives/[a-z0-9]+/page").find(url)?.value?.split(Regex("/"))?.getOrNull(2)
            val fileName = Regex("path=.*").find(url)?.value?.replace("path=", "")
            if (id.isNullOrBlank() || fileName.isNullOrBlank()) {
                return@launch
            }

            val dir = File(context.externalCacheDir, "archive/$id")
            if (!dir.exists()) dir.mkdirs()
            // 文件名携带"/"符号会自动生成父级文件夹
            val finFileName = fileName.replace("/", "_")
            var isExists = dir.listFiles()?.any { it.name == finFileName } == true
            val path = dir.absolutePath + "/$finFileName"

            val result = withContext(Dispatchers.IO) {
                if (!isExists) {
                    kotlin.runCatching {
                        NewHttpHelper.downloadFile(url, path)
                        true
                    }.getOrNull()
                }
            }
            holder.displayImage(path, scaleType)
        }



    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(context).inflate(R.layout.item_reader, parent, false))
    }

    override fun getItemViewType(position: Int, list: List<Pair<String, ScaleType>>): Int {
        return position
    }


    inner class VH(rootView: View) : RecyclerView.ViewHolder(rootView) {

        val rootLayout: TouchToggleLayout = rootView.findViewById(R.id.rootLayout)
        val errorView: TextView = rootView.findViewById(R.id.errorView)
        val progressBar: ProgressBar = rootView.findViewById(R.id.progressBar)
        private var mainView: View? = null

        fun displayImage(filePath: String, scaleType: ScaleType) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)
            when (options.outMimeType.lowercase()) {
                "image/png", "image/jpeg", "image/jpg", "image/webp" -> showCustomPic(filePath, scaleType)
                "image/gif" -> showGif(filePath, scaleType)
                else -> showErrorView()
            }
        }

        private fun showGif(filePath: String, scaleType: ScaleType) {
            mainView = PhotoView(context).also {
                initView(it, scaleType)

                val imageLoader = ImageLoader.Builder(context)
                    .components {
                        if (Build.VERSION.SDK_INT >= 28) {
                            add(ImageDecoderDecoder.Factory())
                        } else {
                            add(coil.decode.GifDecoder.Factory())
                        }
                    }
                    .build()

                it.load(filePath, imageLoader = imageLoader) {
                    crossfade(true)
                    listener(
                        onSuccess = { _, _ ->
                            progressBar.visibility = View.GONE
                            rootLayout.run {
                                setOnClickListener(null)
                                setOnLongClickListener(null)
                            }
                            updateScaleType(it, scaleType)
                        },
                        onError = { _, _ ->
                            showErrorView()
                            it.visibility = View.GONE
                        }
                    )
                    error(R.drawable.bg_error)
                    placeholder(R.drawable.bg_placeholder)
                    size(ViewSizeResolver(it))
                }
            }
        }

        private fun showCustomPic(filePath: String, scaleType: ScaleType) {
            mainView = (if (scaleType == ScaleType.WEBTOON) {
                SubsamplingScaleImageView(context)
            } else {
                WebtoonScaleImageView(context)
            }).also {
                initView(it, scaleType)
                it.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
                    override fun onReady() {
                        super.onReady()
                        progressBar.visibility = View.GONE
                        rootLayout.run {
                            setOnClickListener(null)
                            setOnLongClickListener(null)
                            updateScaleType(it, scaleType)
                        }
                    }
                    override fun onImageLoadError(e: Exception?) {
                        super.onImageLoadError(e)
                        showErrorView()
                        it.visibility = View.GONE
                    }
                })
                it.setImage(ImageSource.uri(filePath))
            }

        }

        @SuppressLint("ClickableViewAccessibility")
        private fun initView(view: View, scaleType: ScaleType) {
            view.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                if (scaleType == ScaleType.WEBTOON) {
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                } else {
                    RelativeLayout.LayoutParams.MATCH_PARENT
                }
            )
//            if (scaleType != ScaleType.WEBTOON) {
                if (view is SubsamplingScaleImageView) {
                    val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            mOnClickListener?.onClick(this@ReaderAdapter, view, -1)
                            return true
                        }
                    })
                    view.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
                } else if (view is PhotoView) {
                    view.setOnViewTapListener { _, _, _ -> mOnClickListener?.onClick(this@ReaderAdapter, view, -1) }
                }
//            }
            view.setOnLongClickListener { mOnLongClickListener?.onLongClick(this@ReaderAdapter, view, -1) == true }
            rootLayout.addView(view)
        }


        private fun showErrorView() {
            errorView.apply {
                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, this, -1) }
                setOnLongClickListener { mOnLongClickListener?.onLongClick(this@ReaderAdapter, this, -1) == true }
                visibility = View.VISIBLE
            }
        }





        fun onScaleTypeChange(scaleType: ScaleType) {
            updateScaleType(mainView, scaleType)
        }

        private fun updateScaleType(imageView: View?, scaleType: ScaleType) {
            when (imageView) {
                is SubsamplingScaleImageView -> {
                    imageView.setMinimumScaleType(when (scaleType) {
                        ScaleType.FIT_WIDTH -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                        ScaleType.FIT_HEIGHT -> SubsamplingScaleImageView.SCALE_TYPE_START
                        ScaleType.FIT_PAGE -> SubsamplingScaleImageView.SCALE_TYPE_CUSTOM
                        ScaleType.WEBTOON -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                    })
                }
                is PhotoView -> {
                    imageView.scaleType = when (scaleType) {
                        ScaleType.FIT_WIDTH -> ImageView.ScaleType.FIT_CENTER
                        ScaleType.FIT_HEIGHT -> ImageView.ScaleType.FIT_START
                        ScaleType.FIT_PAGE -> ImageView.ScaleType.CENTER
                        ScaleType.WEBTOON -> ImageView.ScaleType.FIT_CENTER
                    }
                }
            }
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

