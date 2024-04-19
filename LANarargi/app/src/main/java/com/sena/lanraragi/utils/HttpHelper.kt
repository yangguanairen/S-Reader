package com.sena.lanraragi.utils

import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder


/**
 * FileName: OkHttpHelper
 * Author: JiaoCan
 * Date: 2024/3/22
 */

object HttpHelper {

    private val client by lazy {
        OkHttpClient.Builder()
            .dispatcher(Dispatcher().apply { maxRequests = 5 })
            .build()
    }


    // 主页
    private const val allArchiveUrl = "%host/api/archives"  // 全部档案
    private const val randomArchiveUrl = "%host/api/search/random?count=:count"  // 随机档案


//    private var apiHost = "192.168.0.102:3002"

    // 缩略图链接
    private const val thumbUrl = "%host/api/archives/%s/thumbnail"
    // 搜索链接
    private const val searchUrl = "%host/api/search"
    // 获得漫画解压后的图片列表
    private const val filesUrl = "%host/api/archives/:id/files"
    // 漫画解压后的图片链接
    private const val pathUrl = "%host/api/archives/:id/page?path=:path"


    /**
     * 动态更换url的host
     */
    private fun convertUrl(url: String): String {
        val host = AppConfig.serverHost
        return if (host.startsWith("http://") || host.startsWith("https://")) {
            url.replace("%host", host)
        } else {
            url.replace("%host", "http://$host")
        }
    }

    suspend fun requestAllArchive(): List<Archive>? {
        val finUrl = convertUrl(allArchiveUrl)
        val build = Build().url(finUrl)

        val result = withContext(Dispatchers.IO) {
            val response = build.execute()
            if (response?.code != 200) {
                return@withContext null
            }
            response.body?.byteStream()?.toJSONArray()?.createArchiveList()
        }

        println("测试: 数量${result?.size}")

        return result
    }

    suspend fun requestRandomArchive(count: Int): List<Archive>? {
        val finUrl = convertUrl(randomArchiveUrl).replace(":count", count.toString())
        val mBuilder = Build().url(finUrl)

        val result = withContext(Dispatchers.IO) {
            val response = mBuilder.execute()
            if (response?.code != 200) {
                null
            } else {
                getOrNull { response.body?.byteStream()?.toJSONObject()?.getJSONArray("data")?.createArchiveList() }
            }
        }
        return result
    }



    /**
     * 下载封面图片和漫画内容图片
     */
    suspend fun downloadCore(url: String, savePath: String): Boolean {
        val build = Build().url(url)

        val result = withContext(Dispatchers.IO) {
            val response = build.execute()
            if (response?.code != 200) {
                DebugLog.e("无法下载图片")
                return@withContext false
            }
            val stream = response.body?.byteStream() ?: return@withContext false

            return@withContext getOrNull {
                val fos = FileOutputStream(File(savePath))
                val buffer = ByteArray(1024)
                var len: Int
                while (stream.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.flush()
                fos.close()
                stream.close()

                true
            } ?: false
        }

        return result
    }

    suspend fun getAllPageName(arid: String): List<String>? {
        val url = convertUrl(filesUrl).replace(":id", arid)

        val build = Build().url(url)
        val jsonObject = withContext(Dispatchers.IO) {
            val response = build.execute()
            if (response?.code != 200) return@withContext null
            response.body?.byteStream()?.toJSONObject()
        } ?: return null

        val result = arrayListOf<String>()
        val pages = getOrNull { jsonObject.getJSONArray("pages") } ?: return null
        for (i in 0 until pages.length()) {
            result.add((pages.get(i) as String).replace("./", "${AppConfig.serverHost}/"))
        }

        DebugLog.d("getAllPageName()\n${result.joinToString("\n")} ")

        return result
    }

    suspend fun downloadPath(arid: String, path: String, targetDir: String): Boolean {
        if (!File(targetDir).exists()) {
            DebugLog.e("downloadPath() 目标目录不存在, path: $targetDir")
            return false
        }

        val savePath = File(targetDir, path).absolutePath
        val url = convertUrl(pathUrl).replace(":id", arid).replace(":path", path)

        DebugLog.d("下载图片: $url")
        return downloadCore(url, savePath)
    }


    suspend fun downloadThumb(arid: String, targetDir: String): Boolean {
        if (!File(targetDir).exists()) {
            DebugLog.e("downloadThumb() 目标目录不存在, path: $targetDir")
            return false
        }

        val savePath = File(targetDir, arid).absolutePath
        val url = convertUrl(thumbUrl).replace("%s", arid)
        return downloadCore(url, savePath)
    }


    suspend fun search(query: String, order: LanraragiDB.DBHelper.ORDER, sort: LanraragiDB.DBHelper.SORT): List<Archive>? {

        val encodeQuery = withContext(Dispatchers.IO) {
            URLEncoder.encode(query, "UTF-8")
        }
        val mOrder = if (order == LanraragiDB.DBHelper.ORDER.ASC) "asc" else "desc"
        val mSort = if (sort == LanraragiDB.DBHelper.SORT.TITLE) "title" else "date_added"
        val finalUrl = convertUrl(searchUrl) + "?filter=${encodeQuery}&order=$mSort&sortby=$mOrder&newonly=false&start=-1"

        val build = Build().url(finalUrl)
        val jsonObject = withContext(Dispatchers.IO) {
            val response = build.execute()
            if (response?.code != 200) return@withContext null

            response.body?.byteStream()?.toJSONObject()
        } ?: return null

        val result = withContext(Dispatchers.IO) {
            getOrNull { jsonObject.getJSONArray("data").createArchiveList() }
        }

        return result
    }



    class Build {

        private var url: String? = null
        private val headers: MutableMap<String, String> = mutableMapOf()
        private var isPrintResponseStr = false


        fun url(s: String): Build {
            this.url = s
            return this
        }

        fun addHeader(name: String, value: String): Build {
            this.headers[name] = value
            return this
        }

        fun isPrintRespStr(b: Boolean): Build {
            isPrintResponseStr = b
            return this
        }

        fun execute(): Response? {
            val mUrl = url ?: throw Exception("url is null")
            val build = Request.Builder().url(mUrl)
            val secretKey = AppConfig.serverSecretKey
            if (secretKey.isNotBlank()) {
                build.addHeader("Authorization ", "Bearer $secretKey")
            }
            headers.entries.forEach {
                build.addHeader(it.key, it.value)
            }
            val request = build.build()

            val sTime = System.currentTimeMillis()
            val response = getOrNull { client.newCall(request).execute() }
            val eTime = System.currentTimeMillis()
            val sb = StringBuilder()
                .appendLine("HttpHelper 网络请求日志")
                .appendLine("RequestUrl: $mUrl")
                .appendLine("RequestHeader: ${headers.map { "${it.key}:${it.value}" }.joinToString(", ")}")
                .appendLine("ResponseCode: ${response?.code}")
                .appendLine("请求总耗时: ${eTime - sTime}毫秒")

            DebugLog.d("$sb")

            return response
        }

    }

}

