package com.sena.lanraragi.utils

import android.content.Context
import android.widget.ImageView
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.ViewSizeResolver
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * FileName: NewImageUtils
 * Author: JiaoCan
 * Date: 2024/4/19
 */

object NewImageUtils {

    fun loadThumb(context: Context, arcId: String, imageView: ImageView, onFinish: (() -> Unit)? = null) {
        val dir = File(context.externalCacheDir, "thumb")
        if (!dir.exists()) dir.mkdirs()
        val isExists = dir.listFiles()?.any { it.name == arcId } == true

        val url = AppConfig.serverHost + "/api/archives/$arcId/thumbnail"
        val path = dir.absolutePath + "/$arcId"
        loadCore(url, path, !isExists, imageView, onFinish)
    }

    fun refreshThumb(context: Context, arcId: String, imageView: ImageView, onFinish: (() -> Unit)? = null) {
        val dir = File(context.externalCacheDir, "thumb")
        if (!dir.exists()) dir.mkdirs()

        val url = AppConfig.serverHost + "/api/archives/$arcId/thumbnail"
        val path = dir.absolutePath + "/$arcId"
        loadCore(url, path, true, imageView, onFinish)
    }

    fun loadPre(context: Context, arcId: String, fileName: String, imageView: ImageView, onFinish: (() -> Unit)? = null) {
        val dir = File(context.externalCacheDir, "archive/$arcId")
        if (!dir.exists()) dir.mkdirs()
        // 文件名携带"/"符号会自动生成父级文件夹
        val finFileName = fileName.replace("/", "_")
        val isExists = dir.listFiles()?.any { it.name == finFileName } == true

        val url = AppConfig.serverHost + "/api/archives/$arcId/page?path=$fileName"
        val path = dir.absolutePath + "/$finFileName"
        loadCore(url, path, !isExists, imageView, onFinish)
    }

    private fun loadCore(url: String, path: String, needDownload: Boolean, imageView: ImageView, onFinish: (() -> Unit)? = null) {
        imageView.load(R.drawable.bg_placeholder)
        CoroutineScope(Dispatchers.Main).launch {
            if (needDownload) {
                withContext(Dispatchers.IO) {
                    HttpHelper.downloadCore(url, path)
                }
            }

            imageView.load(path) {
                crossfade(true)
                listener(createListener(url, onFinish))
                error(R.drawable.bg_error)
                placeholder(R.drawable.bg_placeholder)
                size(ViewSizeResolver(imageView))
            }
        }
    }

    private fun createListener(url: String, onFinish: (() -> Unit)? = null) = object : ImageRequest.Listener {
        override fun onSuccess(request: ImageRequest, result: SuccessResult) {
            super.onSuccess(request, result)
            val w = result.drawable.intrinsicWidth
            val h = result.drawable.intrinsicHeight
            DebugLog.e("测试: 实际大小: $url\nw: $w, h: $h")
            onFinish?.invoke()
        }

        override fun onCancel(request: ImageRequest) {
            super.onCancel(request)

            onFinish?.invoke()
        }

        override fun onError(request: ImageRequest, result: ErrorResult) {
            super.onError(request, result)
            DebugLog.e("Coil onError(): \n${result.throwable.stackTraceToString()}")
            onFinish?.invoke()
        }
    }
}

