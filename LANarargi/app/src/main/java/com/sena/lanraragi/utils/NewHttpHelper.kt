package com.sena.lanraragi.utils

import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder


/**
 * FileName: NewHttpHelper
 * Author: JiaoCan
 * Date: 2024/4/18
 */

object NewHttpHelper {

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

    private suspend fun queryAllArchiveFromServer(): List<Archive>? {
        val url = AppConfig.serverHost + "/api/archives"

        val mBuilder = HttpHelper.Build().url(url)
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

        val mBuilder = HttpHelper.Build().url(url)
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

        val mBuilder = HttpHelper.Build().url(url)
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

}

