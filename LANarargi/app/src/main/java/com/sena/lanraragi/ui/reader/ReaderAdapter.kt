package com.sena.lanraragi.ui.reader

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.gifdecoder.GifDecoder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter4.BaseDifferAdapter
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.OnImageEventListener
import com.sena.lanraragi.databinding.ItemReaderBinding
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.HttpHelper
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

class ReaderAdapter : BaseDifferAdapter<Pair<String, ReaderBottomPopup.ScaleType>, ReaderAdapter.VH>(DiffCallback()) {

    private  var mOnClickListener: OnClickListener? = null
    private var mOnLongClickListener: OnLongClickListener? = null

    override fun onBindViewHolder(holder: VH, position: Int, item: Pair<String, ReaderBottomPopup.ScaleType>?) {
        if (item == null) return
        val url = item.first
        val scaleType = item.second

        if (url.isBlank()) {
            holder.bindEmptyView(position)
            return
        }


        val id = Regex("api/archives/[a-z0-9]+/page").find(url)?.value?.split(Regex("/"))?.getOrNull(2)
        val path = Regex("path=.*").find(url)?.value?.replace("path=", "")
        if (id == null || path == null) {
            DebugLog.e("ReaderAdapter.onBindViewHolder(): 无效图链\nurl:$url")
            return
        }
        val dir = File(context.externalCacheDir, "/preview/$id")
        if (!dir.exists()) { dir.mkdirs() }
        val isExists = dir.listFiles()?.any { it.name == path } == true

        // (http://192.168.0.102:3002/api/archives/640c73e21160a84068fbaacc466d1f2f3df54bfc/page?path=bigPicTest/SDCF04.jpg, FIT_WIDTH)
        CoroutineScope(Dispatchers.Main).launch {
            val isSuccess = withContext(Dispatchers.IO) {
                if (!isExists) {
                    return@withContext HttpHelper.downloadPath(id, path, dir.absolutePath)
                } else {
                    return@withContext true
                }
            }
            if (!isSuccess) {
                DebugLog.e("ReaderAdapter.onBindViewHolder(): 下载资源失败\nurl:$item")
                return@launch
            }

            val filePath = dir.absolutePath + "/$path"
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, options)
            val mimeType = options.outMimeType

            when (mimeType.lowercase()) {
                "image/png", "image/jpeg", "image/jpg" -> {
                    holder.bindScaleView(filePath, scaleType, position)
                }
                "image/gif" -> {
                    holder.bindPhotoView(filePath, scaleType, position)
                }
                else -> {
                    holder.bindErrorView(filePath, scaleType, position)
                }
            }

        }
    }



    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(ItemReaderBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemViewType(position: Int, list: List<Pair<String, ReaderBottomPopup.ScaleType>>): Int {
        return position
    }


    inner class VH(private val binding: ItemReaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindPhotoView(filePath: String, scaleType: ReaderBottomPopup.ScaleType, pos: Int) {
            DebugLog.i("ReaderAdapter.VH.bindPhotoView(): 加载资源:$filePath, scaleType:${scaleType.name}")
            val realScaleType = when (scaleType) {
                ReaderBottomPopup.ScaleType.FIT_WIDTH -> ImageView.ScaleType.FIT_CENTER
                ReaderBottomPopup.ScaleType.FIT_HEIGHT -> ImageView.ScaleType.FIT_START
                ReaderBottomPopup.ScaleType.FIT_PAGE -> ImageView.ScaleType.CENTER
                ReaderBottomPopup.ScaleType.WEBTOON -> ImageView.ScaleType.FIT_CENTER
                else -> ImageView.ScaleType.FIT_CENTER
            }
            binding.photoView.apply {
                this.scaleType = realScaleType
                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@ReaderAdapter, it, pos)
                    true
                }
            }
            Glide.with(context)
                .asGif()
                .load(filePath)
                .addListener(object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: GifDrawable, model: Any, target: Target<GifDrawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        binding.imageLayout.removeView(binding.progressBar)
                        binding.errorView.visibility = View.GONE
                        binding.photoView.visibility = View.VISIBLE
                        binding.scaleView.visibility = View.GONE
                        return true
                    }

                })
                .into(binding.photoView)


        }

        fun bindScaleView(filePath: String, scaleType: ReaderBottomPopup.ScaleType, pos: Int) {
            DebugLog.i("ReaderAdapter.VH.bindScaleView(): 加载资源:$filePath, scaleType:${scaleType.name}")
            val realScaleType = when (scaleType) {
                ReaderBottomPopup.ScaleType.FIT_WIDTH -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                ReaderBottomPopup.ScaleType.FIT_HEIGHT -> SubsamplingScaleImageView.SCALE_TYPE_START
                ReaderBottomPopup.ScaleType.FIT_PAGE -> SubsamplingScaleImageView.SCALE_TYPE_CUSTOM
                ReaderBottomPopup.ScaleType.WEBTOON -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                else -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
            }

            binding.scaleView.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
                override fun onReady() {
                    DebugLog.e("测试: onReady")
                    binding.scaleView.visibility = View.INVISIBLE

                }

                override fun onImageLoaded() {
                    DebugLog.e("测试: onImageLoaded")
                    val fadeIn = AlphaAnimation(1f, 0f).apply { duration = 300L }
                    fadeIn.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {

                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            val fadeOut = AlphaAnimation(0f, 1f).apply { duration = 300L }
                            binding.imageLayout.removeView(binding.progressBar)
                            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {
                                    binding.scaleView.visibility = View.VISIBLE
                                }

                                override fun onAnimationEnd(animation: Animation?) {
                                }

                                override fun onAnimationRepeat(animation: Animation?) {
                                }

                            })
                            binding.scaleView.startAnimation(fadeOut)
                        }

                        override fun onAnimationRepeat(animation: Animation?) {

                        }
                    })
                    binding.progressBar.startAnimation(fadeIn)
                }
            })
            binding.scaleView.apply {
                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@ReaderAdapter, it, pos)
                    true
                }
                setMinimumScaleType(realScaleType)
                binding.scaleView.visibility = View.VISIBLE
                binding.errorView.visibility = View.GONE
                binding.photoView.visibility = View.GONE
                setImage(ImageSource.uri(filePath))
            }


//            binding.scaleView.apply {
//                setMinimumScaleType(realScaleType)
//                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, it, pos) }
//                setOnLongClickListener {
//                    mOnLongClickListener?.onLongClick(this@ReaderAdapter, it, pos)
//                    true
//                }
//
//                val fadeIn = AlphaAnimation(1f, 0f)
//                fadeIn.duration = 300L
//                fadeIn.setAnimationListener(object : Animation.AnimationListener {
//                    override fun onAnimationStart(animation: Animation?) {
//                    }
//
//                    override fun onAnimationEnd(animation: Animation?) {
//                        val fadeOut = AlphaAnimation(0f, 1f)
//                        fadeOut.duration = 300L
//                        binding.imageLayout.removeView(binding.progressBar)
//                        binding.errorView.visibility = View.GONE
//                        binding.photoView.visibility = View.GONE
//                        binding.scaleView.visibility = View.VISIBLE
//                        setImage(ImageSource.uri(filePath))
//                        binding.scaleView.startAnimation(fadeOut)
//                    }
//
//                    override fun onAnimationRepeat(animation: Animation?) {
//                    }
//
//                })
//                binding.progressBar.startAnimation(fadeIn)
//
//
//
//            }


        }

        fun bindErrorView(filePath: String, scaleType: ReaderBottomPopup.ScaleType, pos: Int) {
            DebugLog.e("ReaderAdapter.VH.bindScaleView(): 不支持的资源:$filePath, scaleType:${scaleType.name}")
            binding.errorView.apply {
                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@ReaderAdapter, it, pos)
                    true
                }
            }

            binding.imageLayout.removeView(binding.progressBar)
            binding.errorView.visibility = View.VISIBLE
            binding.photoView.visibility = View.GONE
            binding.scaleView.visibility = View.GONE
        }

        fun bindEmptyView(pos: Int) {
            binding.imageLayout.apply {
                setOnClickListener { mOnClickListener?.onClick(this@ReaderAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@ReaderAdapter, it, pos)
                    true
                }
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.errorView.visibility = View.GONE
            binding.photoView.visibility = View.GONE
            binding.scaleView.visibility = View.GONE
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Pair<String, ReaderBottomPopup.ScaleType>>() {
        override fun areItemsTheSame(oldItem: Pair<String, ReaderBottomPopup.ScaleType>, newItem: Pair<String, ReaderBottomPopup.ScaleType>): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(oldItem: Pair<String, ReaderBottomPopup.ScaleType>, newItem: Pair<String, ReaderBottomPopup.ScaleType>): Boolean {
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

