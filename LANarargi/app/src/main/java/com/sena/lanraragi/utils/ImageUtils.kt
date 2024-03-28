package com.sena.lanraragi.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.sena.lanraragi.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * FileName: ImageUtils
 * Author: JiaoCan
 * Date: 2024/3/25
 */

object ImageUtils {

    fun loadThumbTo2View(context: Context, arcid: String, imageView1: ImageView, imageView2: ImageView) {
        val dir = File(context.externalCacheDir, "thumb")
        if (!dir.exists()) dir.mkdirs()
        val path = dir.absolutePath + "/$arcid"
        val isExists = dir.listFiles()?.any { it.name == arcid } == true

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                if (!isExists) {
                    return@withContext HttpHelper.downloadThumb(arcid, dir.absolutePath)
                } else {
                    return@withContext true
                }
            }
            if (!result) return@launch


            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)
            val w = options.outWidth
            val h  = options.outHeight
            val size = w / h.toFloat()
            DebugLog.d("测试：path: $path\nw: $w, h: $h, 比例: $size")

            val builder = Glide.with(context).load(path)
                .error(R.drawable.bg_error)
                .placeholder(R.drawable.bg_placeholder)
                .transition(withCrossFade(500))
            if (imageView1.isAttachedToWindow == false || imageView2.isAttachedToWindow == false) {
                DebugLog.e("loadThumbTo2View() 视图已经从窗口移除")
                return@launch
            }
            if (size > 1f) {
                imageView1.visibility = View.GONE
                imageView2.visibility = View.VISIBLE
                builder.into(imageView2)
            } else {
                imageView1.visibility = View.VISIBLE
                imageView2.visibility = View.GONE
                builder.into(imageView1)
            }

        }
    }


    fun loadThumb(context: Context, arcid: String, imageView: ImageView) {
        val thumbCacheDir = File(context.externalCacheDir, "thumb")
        if (!thumbCacheDir.exists()) thumbCacheDir.mkdirs()
        val thumbCachePath = thumbCacheDir.absolutePath + "/${arcid}"
        val isExists = thumbCacheDir.listFiles()?.any { it.name == arcid } == true

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                if (!isExists) {
                    return@withContext HttpHelper.downloadThumb(arcid, thumbCacheDir.absolutePath)
                } else {
                    return@withContext true
                }
            }
            if (!result) return@launch
            if (imageView.isAttachedToWindow == false) {
                DebugLog.e("loadThumb() 视图已经从窗口移除")
                return@launch
            }
            Glide.with(context).load(thumbCachePath)
                .error(R.drawable.bg_error)
                .placeholder(R.drawable.bg_placeholder)
                .transition(withCrossFade(500))
                .into(imageView)
        }
    }

    fun loadPreview(context: Context, arcid: String, path: String, imageView: ImageView) {
        val dir = File(context.externalCacheDir, "/preview/$arcid")
        if (!dir.exists()) {
            val isSuccess = dir.mkdirs()
            DebugLog.d("loadPreview() isSuccess: $isSuccess")
        }
//
        val isExists = dir.listFiles()?.any { it.name == path } == true

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                if (!isExists) {
                    return@withContext HttpHelper.downloadPath(arcid, path, dir.absolutePath)
                } else {
                    return@withContext true
                }
            }
            if (!result) return@launch

            val file = dir.absolutePath + "/$path"
//            if (imageView.isAttachedToWindow == false) {
//                DebugLog.e("loadPreview() 视图已经从窗口移除")
//                return@launch
//            }
            Glide.with(context).load(file)
                .error(R.drawable.bg_error)
                .placeholder(R.drawable.bg_placeholder)
                .transition(withCrossFade(500))
                .into(imageView)
        }
    }


    fun loadPath(context: Context, arcid: String, path: String, imageView: PhotoView) {
        val dir = File(context.externalCacheDir, "/preview/$arcid")
        if (!dir.exists()) {
            val isSuccess = dir.mkdirs()
            DebugLog.d("loadPreview() isSuccess: $isSuccess")
        }
//
        val isExists = dir.listFiles()?.any { it.name == path } == true

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                if (!isExists) {
                    return@withContext HttpHelper.downloadPath(arcid, path, dir.absolutePath)
                } else {
                    return@withContext true
                }
            }
            if (!result) return@launch

            val file = dir.absolutePath + "/$path"
//            if (imageView.isAttachedToWindow == false) {
//                DebugLog.e("loadPreview() 视图已经从窗口移除")
//                return@launch
//            }
            Glide.with(context).load(file)
                .error(R.drawable.bg_error)
                .placeholder(R.drawable.bg_placeholder)
                .transition(withCrossFade(500))
                .into(imageView)
        }
    }

}

