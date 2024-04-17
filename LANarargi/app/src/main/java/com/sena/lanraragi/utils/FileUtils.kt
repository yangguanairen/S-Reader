package com.sena.lanraragi.utils

import android.content.Context
import java.io.File


/**
 * FileName: FileUtils
 * Author: JiaoCan
 * Date: 2024/4/16
 */

object FileUtils {


    // 返回MB单位
    fun getCacheSize(context: Context): Long {
        val dir = context.externalCacheDir ?: return 0
        val size = getDirSize(dir)
        val mbSize = size / 1024 / 8
        return mbSize
    }

    private fun getDirSize(dir: File): Long {
        var size = 0L
        val files = dir.listFiles()
        if (files == null) { // 4.2的模拟器空指针
            return 0
        }

        files.forEach { f ->
            if (f.isDirectory) {
                size += getDirSize(f)
            } else {
                size += f.length()
            }
        }
        return size

    }

    fun clearAllCache(context: Context) {
        val dir = context.externalCacheDir ?: return
        dir.listFiles()?.forEach { f ->
            getOrNull {
                val result = f.deleteRecursively()
                if (result == false) {
                    DebugLog.e("clearAllCache() 删除失败, 路径: ${f.absolutePath}")
                }
            }
        }
    }


}

