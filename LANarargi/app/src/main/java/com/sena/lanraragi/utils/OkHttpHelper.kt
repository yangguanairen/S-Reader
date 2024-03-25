package com.sena.lanraragi.utils

import com.sena.lanraragi.database.archiveData.Archive
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder


/**
 * FileName: OkHttpHelper
 * Author: JiaoCan
 * Date: 2024/3/22
 */

object OkHttpHelper {

    private val client by lazy {
        OkHttpClient.Builder()
            .dispatcher(Dispatcher().apply { maxRequests = 5 })
            .build()
    }

    private const val allArchive = "http://192.168.0.102:3002/api/archives"



    suspend fun requestAllArchive(): List<Archive>? {
        val request = Request.Builder()
            .url(allArchive)
            .build()
        val response = client.newCall(request).execute()

        if (response.code != 200) return null

        val s = response.body?.string()
        val result = kotlin.runCatching {
            val rootArray = JSONArray(s)
            val archiveList = arrayListOf<Archive>()
            for (i in 0 until rootArray.length()) {
                val aItem = rootArray.get(i) as JSONObject
                val arcid = kotlin.runCatching { aItem.getString("arcid") }.getOrNull() ?: continue
                val isnew = kotlin.runCatching { aItem.getBoolean("isnew") }.getOrNull()
                val extension = kotlin.runCatching { aItem.getString("extension") }.getOrNull()
                val pagecount = kotlin.runCatching { aItem.getInt("pagecount") }.getOrNull()
                val progress = kotlin.runCatching { aItem.getInt("progress") }.getOrNull()
                val tags = kotlin.runCatching { aItem.getString("tags") }.getOrNull()
                val lastreadtime = kotlin.runCatching { aItem.getLong("lastreadtime") }.getOrNull()
                val title = kotlin.runCatching { aItem.getString("title") }.getOrNull()
                val dataAdded = findAddtime(parseTags(tags ?: ""))

                val archive = Archive(arcid, isnew, extension, pagecount,
                    progress, tags, lastreadtime, title, dataAdded)
                archiveList.add(archive)
            }

            archiveList
        }.onFailure { it.printStackTrace() }.getOrNull()


        println("测试: 数量")

        return result
    }

    private fun parseTags(s: String): List<Pair<String, String>> {
        val regex = Regex(",\\s*")
        val tagList = s.split(regex).map {
            val itemArr = it.split(Regex(":"), 2)
            if (itemArr.size == 2) {
                Pair(itemArr[0], itemArr[1])
            } else {
                Pair("未知", it)
            }
        }
        return tagList
    }

    private fun findAddtime(list: List<Pair<String, String>>): Long {
        return list.filter { it.first == "date_added" }.firstOrNull()?.second?.toLong() ?: -1L
    }


//
//    companion object Builder {
//
//        private var url: String = ""
//
//        private val
//
//
//        fun setUrl(s: String): Builder {
//            url = s
//            return this
//        }
//
//        fun post()
//
//    }
}

