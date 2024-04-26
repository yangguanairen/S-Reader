package com.sena.lanraragi.utils

import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder


/**
 * FileName: NewHttpHelper
 * Author: JiaoCan
 * Date: 2024/4/18
 */

object NewHttpHelper {

    private val defaultClient by lazy {
        OkHttpClient.Builder()
            .dispatcher(Dispatcher().apply { maxRequests = 3 })
            .build()
    }

    suspend fun queryAllArchive(): List<Archive> {
        val serverResult = queryAllArchiveFromServer()
        if (serverResult == null) {
            DebugLog.e("请求失败，不更新数据库")
        } else {
            LanraragiDB.updateArchiveList(serverResult)
        }
        return serverResult ?: arrayListOf()
    }


    suspend fun queryArchiveByTag(query: String): List<Archive> {
        val isUseLocalSearch = AppConfig.enableLocalSearch

        var result: List<Archive>? = null
        if (!isUseLocalSearch && query.isNotBlank()) { // 空白内容默认拉取本地数据库所有内容
            queryArchiveByTagFromServer(query)?.let { result = it }
        }
        if (result == null || isUseLocalSearch) {
            result = LanraragiDB.queryArchivesWithTag(query)
        }
        return result ?: arrayListOf()
    }

    suspend fun getRandomArchive(specifyCount: Int = -1): List<Archive> {
        // val isUseLocalSearch = AppConfig.enableLocalSearch
        var count = AppConfig.randomCount
        if (count <= 0) count = 1
        if (specifyCount > 0) count = specifyCount

        // var result: List<Archive>? = null
        // if (!isUseLocalSearch) {
        //     getRandomArchiveFromServer(count)?.let { result = it }
        // }
        // if (result == null || isUseLocalSearch) {
        //     result = LanraragiDB.getRandomArchive(count)
        // }
        // 2024.04.19 随机档案改为只从本地数据库获取
        // 避免获取时间过长
        val result = LanraragiDB.getRandomArchive(count)
        return result
    }
    
    suspend fun extractManga(id: String): ArrayList<String>? {
        val url = AppConfig.serverHost + "/api/archives/$id/files?force=true"

        val mBuilder = Build().url(url)
        var result: ArrayList<String>? = null
        withContext(Dispatchers.IO) {
            val response = mBuilder.execute()
            if (response?.code != 200) return@withContext
            getOrNull {
                val jsonObject = response.body?.byteStream()?.toJSONObject()
                val pages = jsonObject?.getJSONArray("pages")
                if (pages != null) {
                    val list = arrayListOf<String>()
                    for (i in 0 until pages.length()) {
                        list.add((pages.get(i) as String).replace("./", "${AppConfig.serverHost}/"))
                    }
                    result = list
                }
            }
        }
        return result
    }

    /**
     * 注意不捕获异常
     */
    suspend fun downloadFile(url: String, savePath: String, onDownloadProgress: ((curSize: Int, totalSize: Int) -> Unit)? = null) {
        val build = Build().url(url)

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(Interceptor { chain ->
                val originalResp = chain.proceed(chain.request())
                originalResp.newBuilder()
                    .body(ProgressResponseBody(originalResp.body!!, onDownloadProgress))
                    .build()
            })
            .build()
        build.addClient(client)

        withContext(Dispatchers.IO) {
            val response = build.execute()
            if (response?.code != 200) {
                DebugLog.e("无法下载文件: $url")
                return@withContext
            }
            val stream = response.body?.byteStream() ?: return@withContext

            val fos = FileOutputStream(File(savePath))
            val buffer = ByteArray(1024)
            var len: Int
            while (stream.read(buffer).also { len = it } > 0) {
                fos.write(buffer, 0, len)
            }
            fos.flush()
            fos.close()
            stream.close()
        }
    }
    
    
    private suspend fun queryAllArchiveFromServer(): List<Archive>? {
        val url = AppConfig.serverHost + "/api/archives"

        val mBuilder = Build().url(url)
        var result: List<Archive>? = null
        withContext(Dispatchers.IO) {
            val response = mBuilder.execute()
            if (response?.code != 200) return@withContext
            getOrNull {
                result = response.body?.byteStream()?.toJSONArray()?.createArchiveList()
            }
        }
        return result
    }


    private suspend fun queryArchiveByTagFromServer(query: String): List<Archive>? {
        val encodeQuery = withContext(Dispatchers.IO) {
            URLEncoder.encode(query, "UTF-8")
        }
        val orderStr = if (AppConfig.order == LanraragiDB.DBHelper.ORDER.ASC) "asc" else "desc"
        val sortStr = if (AppConfig.sort == LanraragiDB.DBHelper.SORT.TIME) "date_added" else "title"
        val isNewStr = if (AppConfig.isNew) "true" else "false"
        val url = StringBuilder().append(AppConfig.serverHost + "/api/search")
            .append("?filter=$encodeQuery")
            .append("&order=$orderStr")
            .append("&sortby=$sortStr")
            .append("&newonly=$isNewStr")
            .append("&start=-1")
            .toString()

        val mBuilder = Build().url(url)
        var result: List<Archive>? = null
        withContext(Dispatchers.IO) {
            val response = mBuilder.execute()
            if (response?.code != 200) return@withContext
            getOrNull {
                result = response.body?.byteStream()?.toJSONObject()?.getJSONArray("data")?.createArchiveList()
            }
        }
        return result
    }

    private suspend fun getRandomArchiveFromServer(count: Int): List<Archive>? {
        val url = AppConfig.serverHost + "/api/search/random?count=$count"

        val mBuilder = Build().url(url)
        var result: List<Archive>? = null
        withContext(Dispatchers.IO) {
            val response = mBuilder.execute()
            if (response?.code != 200) return@withContext
            getOrNull {
                result = response.body?.byteStream()?.toJSONObject()?.getJSONArray("data")?.createArchiveList()
            }
        }
        return result
    }

    class ProgressResponseBody(respBody: ResponseBody, onDownloadProgress: ((curSize: Int, totalSize: Int) -> Unit)? = null) : ResponseBody() {

        private val mRespBody = respBody
        private val mListener = onDownloadProgress
        private var bufferedSource: BufferedSource? = null
        private var lastProgress: Int = 0

        override fun contentLength(): Long = mRespBody.contentLength()
        override fun contentType(): MediaType? = mRespBody.contentType()

        override fun source(): BufferedSource = bufferedSource ?: source(mRespBody.source()).buffer()
            .also { bufferedSource = it }


        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead = 0L
                override fun read(sink: Buffer, byteCount: Long): Long {

                    val bytesRead = super.read(sink, byteCount)
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0L
                    val curProgress = (totalBytesRead * 100 / contentLength()).toInt()
                    if (curProgress != lastProgress) {
                        lastProgress = curProgress
                        mListener?.invoke(curProgress, 100)
                    }
                    return bytesRead
                }
            }
        }

    }



    class Build {

        private var url: String? = null
        private val headers: MutableMap<String, String> = mutableMapOf()
        private var isPrintResponseStr = false
        private var mClient: OkHttpClient? = null


        fun url(s: String): Build {
            this.url = s
            return this
        }

        fun addClient(c: OkHttpClient): Build {
            mClient = c
            return this
        }

        fun execute(): Response? {
            val mUrl = url ?: throw Exception("url is null")
            val build = Request.Builder().url(mUrl)
            val secretKey = AppConfig.serverSecretKey
            if (secretKey.isNotBlank()) {
                headers["Authorization"] = secretKey
            }
            headers.entries.forEach {
                build.addHeader(it.key, it.value)
            }
            val request = build.build()
            val finalClient = mClient ?: defaultClient

            val sTime = System.currentTimeMillis()
            val response = getOrNull { finalClient.newCall(request).execute() }
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

