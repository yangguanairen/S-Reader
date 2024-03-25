package com.sena.lanraragi

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.utils.OkHttpHelper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.sena.lanraragi", appContext.packageName)
    }


    @Test
    fun testHttp() {

        val request = Request.Builder()
            .url("http://192.168.0.102:3002/api/archives")
            .build()
        val response = OkHttpClient.Builder().build().newCall(request).execute()

        if (response.code != 200) {

        }

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
//                val dataAdded = OkHttpHelper.findAddtime(OkHttpHelper.parseTags(tags ?: ""))

                val archive = Archive(arcid, isnew, extension, pagecount,
                    progress, tags, lastreadtime, title, null)
                archiveList.add(archive)
            }

            archiveList
        }.onFailure { it.printStackTrace() }.getOrNull()


        println("测试: 数量${result?.size}")
    }
}