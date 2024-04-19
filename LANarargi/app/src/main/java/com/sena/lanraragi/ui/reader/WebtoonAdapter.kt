package com.sena.lanraragi.ui.reader

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.load
import com.chad.library.adapter4.BaseQuickAdapter
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.sena.lanraragi.databinding.ItemReaderWebtoonBinding
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.HttpHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * FileName: WebtoonAdapter
 * Author: JiaoCan
 * Date: 2024/4/16
 */

class WebtoonAdapter : BaseQuickAdapter<String, WebtoonAdapter.VH>() {

    private  var mOnClickListener: OnClickListener? = null
    private var mOnLongClickListener: OnLongClickListener? = null


    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        if (item == null) return
        val url = item

        if (url.isBlank()) {
            holder.bindEmptyView(position)
            return
        }


        val id = Regex("api/archives/[a-z0-9]+/page").find(url)?.value?.split(Regex("/"))?.getOrNull(2)
        val path = Regex("path=.*").find(url)?.value?.replace("path=", "")
        val finFileName = path?.replace("/", "_")
        if (id == null || path == null) {
            DebugLog.e("ReaderAdapter.onBindViewHolder(): 无效图链\nurl:$url")
            return
        }
        val dir = File(context.externalCacheDir, "/archive/$id")
        if (!dir.exists()) { dir.mkdirs() }
        val isExists = dir.listFiles()?.any { it.name == path } == true

        // (http://192.168.0.102:3002/api/archives/640c73e21160a84068fbaacc466d1f2f3df54bfc/page?path=bigPicTest/SDCF04.jpg, FIT_WIDTH)
        CoroutineScope(Dispatchers.Main).launch {
            val isSuccess = withContext(Dispatchers.IO) {
                if (!isExists) {
                    return@withContext HttpHelper.downloadCore(url, dir.absolutePath + "/$finFileName")
                } else {
                    return@withContext true
                }
            }
            if (!isSuccess) {
                DebugLog.e("ReaderAdapter.onBindViewHolder(): 下载资源失败\nurl:$item")
                return@launch
            }

            val filePath = dir.absolutePath + "/$finFileName"
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, options)
            val mimeType = options.outMimeType

            when (mimeType.lowercase()) {
                "image/png", "image/jpeg", "image/jpg", "image/webp" -> {
                    holder.bindScaleView(filePath, position)
                }
                "image/gif" -> {
                    holder.bindPhotoView(filePath, position)
                }
                else -> {
                    holder.bindErrorView(filePath, position)
                }
            }

        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(ItemReaderWebtoonBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemViewType(position: Int, list: List<String>): Int {
        return position
    }

    inner class VH(private val binding: ItemReaderWebtoonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindPhotoView(filePath: String, pos: Int) {
            DebugLog.i("ReaderAdapter.VH.bindPhotoView(): 加载资源:$filePath")
            binding.photoView.apply {
                this.scaleType = ImageView.ScaleType.FIT_CENTER
                setOnClickListener { mOnClickListener?.onClick(this@WebtoonAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@WebtoonAdapter, it, pos)
                    true
                }
            }
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(coil.decode.GifDecoder.Factory())
                    }
                }
                .build()
            binding.photoView.load(filePath, imageLoader = imageLoader) {
                listener { request, result ->
                    DebugLog.e("测试： gif加载完成: $filePath")
                    binding.imageLayout.removeView(binding.progressBar)
                    binding.errorView.visibility = View.GONE
                    binding.photoView.visibility = View.VISIBLE
                    binding.scaleView.visibility = View.GONE
                }
            }
        }


        fun bindScaleView(filePath: String, pos: Int) {
            DebugLog.i("ReaderAdapter.VH.bindScaleView(): 加载资源:$filePath")
            binding.scaleView.apply {
                setOnClickListener { mOnClickListener?.onClick(this@WebtoonAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@WebtoonAdapter, it, pos)
                    true
                }
                setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
                binding.scaleView.visibility = View.VISIBLE
                binding.errorView.visibility = View.GONE
                binding.photoView.visibility = View.GONE
                setImage(ImageSource.uri(filePath))
            }
        }

        fun bindErrorView(filePath: String, pos: Int) {
            DebugLog.e("ReaderAdapter.VH.bindScaleView(): 不支持的资源:$filePath")
            binding.errorView.apply {
                setOnClickListener { mOnClickListener?.onClick(this@WebtoonAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@WebtoonAdapter, it, pos)
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
                setOnClickListener { mOnClickListener?.onClick(this@WebtoonAdapter, it, pos) }
                setOnLongClickListener {
                    mOnLongClickListener?.onLongClick(this@WebtoonAdapter, it, pos)
                    true
                }
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.errorView.visibility = View.GONE
            binding.photoView.visibility = View.GONE
            binding.scaleView.visibility = View.GONE
        }

    }

    fun setOnImageClickListener(func: (a: WebtoonAdapter, v: View, p: Int) -> Unit)  {
        mOnClickListener = object : OnClickListener {
            override fun onClick(a: WebtoonAdapter, v: View, p: Int) {
                func.invoke(a, v, p)
            }
        }
    }

    fun setOnImageLongClickListener(func: (a: WebtoonAdapter, v: View, p: Int) -> Boolean)  {
        mOnLongClickListener = object : OnLongClickListener {
            override fun onLongClick(a: WebtoonAdapter, v: View, p: Int): Boolean {
                return func.invoke(a, v, p)
            }
        }
    }

    private interface OnClickListener {
        fun onClick(a: WebtoonAdapter, v: View, p: Int)
    }

    private interface OnLongClickListener {
        fun onLongClick(a: WebtoonAdapter, v: View, p: Int): Boolean
    }


}

