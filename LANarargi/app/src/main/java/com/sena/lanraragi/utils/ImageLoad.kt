package com.sena.lanraragi.utils

import android.content.Context
import android.os.Build
import android.widget.ImageView
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.ViewSizeResolver
import coil.transform.RoundedCornersTransformation
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception


/**
 * FileName: ImageLoadBuilder
 * Author: JiaoCan
 * Date: 2024/4/21
 */

class ImageLoad private constructor(context: Context) {

    private val mContext = context
    private var arcId: String = ""
    private var previewIndex: String = ""
    private var picUrl: String = ""
    private var isThumb: Boolean = false
    private var isPic: Boolean = false
    private var isPreview: Boolean = false
    private var isIgnoreDiskCache: Boolean = false
    private var imageView: ImageView? = null
    private var subScaleImageView: SubsamplingScaleImageView? = null
    private var cornerRadius: Float = 0f

    private var mOnStartListener: Builder.OnStartListener? = null
    private var mOnSuccessListener: Builder.OnSuccessListener? = null
    private var mOnErrorListener: Builder.OnErrorListener? = null
    private var mOnProgressListener: ((curSize: Int, totalSize: Int) -> Unit)? = null

    class Builder(context: Context) {

        private val imageLoad: ImageLoad = ImageLoad(context)

        fun loadThumb(id: String): Builder {
            imageLoad.arcId = id
            imageLoad.isThumb = true
            return this
        }

        fun loadPic(s: String): Builder {
            imageLoad.picUrl = s
            imageLoad.isPic = true
            return this
        }

        fun loadPreview(id: String, index: String): Builder {
            imageLoad.arcId = id
            imageLoad.previewIndex = index
            imageLoad.isPreview = true
            return this
        }

        fun isIgnoreDiskCache(b: Boolean): Builder {
            imageLoad.isIgnoreDiskCache = b
            return this
        }

        fun setRadius(radius: Float): Builder {
            imageLoad.cornerRadius = radius
            return this
        }

        fun into(v: ImageView): ImageLoad {
            imageLoad.imageView = v
            return imageLoad
        }

        fun into(v: SubsamplingScaleImageView): ImageLoad {
            imageLoad.subScaleImageView = v
            return imageLoad
        }

        fun doOnStart(func: () -> Unit): Builder {
            imageLoad.mOnStartListener = object : OnStartListener {
                override fun onStart() {
                    func.invoke()
                }
            }
            return this
        }

        /**
         * 会同时覆盖掉onSuccess和onError的回调
         */
        fun doOnFinish(func: () -> Unit): Builder {
            imageLoad.mOnSuccessListener = object : OnSuccessListener {
                override fun onSuccess() {
                    func.invoke()
                }
            }
            imageLoad.mOnErrorListener = object : OnErrorListener {
                override fun onError() {
                    func.invoke()
                }
            }
            return this
        }

        fun doOnSuccess(func: () -> Unit): Builder {
            imageLoad.mOnSuccessListener = object : OnSuccessListener {
                override fun onSuccess() {
                    func.invoke()
                }
            }
            return this
        }

        fun doOnError(func: () -> Unit): Builder {
            imageLoad.mOnErrorListener = object : OnErrorListener {
                override fun onError() {
                    func.invoke()
                }
            }
            return this
        }

        fun doOnProgressChange(func: (curSize: Int, totalSize: Int) -> Unit): Builder {
            imageLoad.mOnProgressListener = func
            return this
        }

        interface OnStartListener {
            fun onStart()
        }

        interface OnSuccessListener {
            fun onSuccess()
        }

        interface OnErrorListener {
            fun onError()
        }
    }

    fun execute() {
        if (isThumb) {
            mOnStartListener?.onStart()
            loadThumb()
        } else if (isPreview) {
            mOnStartListener?.onStart()
            loadPreview()
        } else if (isPic) {
            mOnStartListener?.onStart()
            loadPic()
        } else {
            DebugLog.e("不支持加载其他图片")
        }
    }


    private fun loadThumb() {
        val dir = File(mContext.externalCacheDir, "thumb")
        if (!dir.exists()) dir.mkdirs()
        var isExists = dir.listFiles()?.any { it.name == arcId } == true
        if (isIgnoreDiskCache) isExists = false

        val url = AppConfig.serverHost + "/api/archives/$arcId/thumbnail"
        val path = dir.absolutePath + "/$arcId"

        imageView?.load(R.drawable.bg_placeholder)

        CoroutineScope(Dispatchers.Main).launch {
            if (!isExists) {
                withContext(Dispatchers.IO) {
                    kotlin.runCatching {
                        NewHttpHelper.downloadFile(url, path, mOnProgressListener)
                    }.onFailure {
                        mOnErrorListener?.onError()
                    }
                }
            }
            imageView?.let { v ->
                v.load(path) {
                    crossfade(true)
                    listener(createListener(url))
                    error(R.drawable.bg_error)
//                    placeholder(R.drawable.bg_placeholder)
                    size(ViewSizeResolver(v))
                    if (cornerRadius > 0f) {
                        transformations(RoundedCornersTransformation(cornerRadius))
                    }
                }
            }
        }
    }

    private fun loadPreview() {
        val dir = File(mContext.externalCacheDir, "archiveThumb/$arcId")
        if (!dir.exists()) dir.mkdirs()
        var isExists = dir.listFiles()?.any { it.name == previewIndex } == true
        if (isIgnoreDiskCache) isExists = false

        // http://192.168.0.102:3003/api/archives/f469625f2b9af02827575a0e743d8244df5378cf/thumbnail?page=5
        val url = AppConfig.serverHost + "/api/archives/$arcId/thumbnail?page=$previewIndex"
        val path = dir.absolutePath + "/$previewIndex"

        imageView?.load(R.drawable.bg_placeholder)

        CoroutineScope(Dispatchers.Main).launch {
            if (!isExists) {
                withContext(Dispatchers.IO) {
                    kotlin.runCatching {
                        NewHttpHelper.downloadFile(url, path, mOnProgressListener)
                    }.onFailure {
                        mOnErrorListener?.onError()
                    }
                }
            }
            imageView?.let { v ->
                v.load(path) {
                    crossfade(true)
                    listener(createListener(url))
                    error(R.drawable.bg_error)
                    placeholder(R.drawable.bg_placeholder)
                    size(ViewSizeResolver(v))
                    if (cornerRadius > 0f) {
                        transformations(RoundedCornersTransformation(cornerRadius))
                    }
                }
            }
        }
    }


    private fun loadPic() {
        val id = Regex("api/archives/[a-z0-9]+/page").find(picUrl)?.value?.split(Regex("/"))?.getOrNull(2)
        val fileName = Regex("path=.*").find(picUrl)?.value?.replace("path=", "")
        if (id.isNullOrBlank() || fileName.isNullOrBlank()) {
            mOnErrorListener?.onError()
            return
        }

        val dir = File(mContext.externalCacheDir, "archive/$id")
        if (!dir.exists()) dir.mkdirs()
        // 文件名携带"/"符号会自动生成父级文件夹
        val finFileName = fileName.replace("/", "_")
        var isExists = dir.listFiles()?.any { it.name == finFileName } == true
        if (isIgnoreDiskCache) isExists = false

        val url = picUrl
        val path = dir.absolutePath + "/$finFileName"

        CoroutineScope(Dispatchers.Main).launch {
            if (!isExists) {
                withContext(Dispatchers.IO) {
                    kotlin.runCatching {
                        NewHttpHelper.downloadFile(url, path, mOnProgressListener)
                    }.onFailure {
                        mOnErrorListener?.onError()
                    }
                }
            }
            if (imageView != null) {
                loadPicWithImageView(path)
            } else if (subScaleImageView != null) {
                loadPicWithScaleView(path)
            }
        }
    }

    private fun loadPicWithImageView(path: String) {
        val view = imageView ?: return
        val imageLoader = ImageLoader.Builder(mContext)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(coil.decode.GifDecoder.Factory())
                }
            }
            .build()

        view.load(path, imageLoader = imageLoader) {
            crossfade(true)
            listener(createListener(path))
            error(R.drawable.bg_error)
            placeholder(R.drawable.bg_placeholder)
            size(ViewSizeResolver(view))
        }
    }

    private fun loadPicWithScaleView(path: String) {
        val view = subScaleImageView ?: return
        view.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onReady() {

            }

            override fun onImageLoaded() {
                mOnSuccessListener?.onSuccess()
            }

            override fun onPreviewLoadError(e: Exception?) {
            }

            override fun onImageLoadError(e: Exception?) {
            }

            override fun onTileLoadError(e: Exception?) {
                mOnErrorListener?.onError()
            }

            override fun onPreviewReleased() {
            }

        })
        view.setImage(ImageSource.uri(path))
    }

    private fun createListener(url: String) = object : ImageRequest.Listener {
        override fun onSuccess(request: ImageRequest, result: SuccessResult) {
            super.onSuccess(request, result)
//            val w = result.drawable.intrinsicWidth
//            val h = result.drawable.intrinsicHeight
//            DebugLog.d("测试: 实际大小: $url\nw: $w, h: $h")
            mOnSuccessListener?.onSuccess()
        }

        override fun onCancel(request: ImageRequest) {
            super.onCancel(request)
            mOnErrorListener?.onError()
        }

        override fun onError(request: ImageRequest, result: ErrorResult) {
            super.onError(request, result)
            DebugLog.e("Coil onError(): \n${result.throwable.stackTraceToString()}")
            mOnErrorListener?.onError()
        }
    }


}

