package com.sena.lanraragi.utils

import android.content.Context
import android.util.TypedValue
import com.sena.lanraragi.database.archiveData.Archive
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream


/**
 * FileName: ExpandFunc
 * Author: JiaoCan
 * Date: 2024/3/26
 */


fun InputStream.toJSONObject(): JSONObject? {
    val reader = BufferedReader(reader())
    val sb = StringBuilder()
    var line = reader.readLine()
    while (line != null) {
        sb.append(line)
        line = reader.readLine()
    }
    return getOrNull { JSONObject(sb.toString()) }
}

fun InputStream.toJSONArray(): JSONArray? {
    val reader = BufferedReader(reader())
    val sb = StringBuilder()
    var line = reader.readLine()
    while (line != null) {
        sb.append(line)
        line = reader.readLine()
    }
    return getOrNull { JSONArray(sb.toString()) }
}



fun JSONObject.createArchive(): Archive? {
    var arcId = getOrNull { getString("arcid") }
    if (arcId == null) {
        arcId = getOrNull { getString("id") }
    }
    if (arcId == null) {
        DebugLog.e("序列化失败，无法找到arcid，对象: ${ toString()}")
        return null
    }
    val isNew = getOrNull { getBoolean("isnew") }
    val extension = getOrNull { getString("extension") }
    val pageCount = getOrNull { getInt("pagecount") }
    val progress = getOrNull { getInt("progress") }
    val tags = getOrNull { getString("tags") }
    val lastReadTime = getOrNull { getLong("lastreadtime") }
    val title = getOrNull { getString("title") }
    val dataAdded = parseTags(tags ?: "").findAddTime()

    val archive = Archive(arcId, isNew, extension, pageCount, progress,
        tags, lastReadTime, title, dataAdded)

    return archive
}

fun JSONArray.createArchiveList(): List<Archive> {
    val result = arrayListOf<Archive>()
    for (i in 0 until length()) {
        val item = get(i) as JSONObject
        val archive = item.createArchive()
        archive?.let { result.add(it) }
    }
    return result
}

fun parseTags(s: String): List<Pair<String, String>> {
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

fun List<Pair<String, String>>.findAddTime(): Long {
    return firstOrNull { it.first == "date_added" }?.second?.toLong() ?: -1L
}

fun <T> getOrNull(func: () -> T?): T? {
    return runCatching {
        func.invoke()
    }.getOrNull()
}


fun Context.getThemeColor(id: Int): Int? {
    val typedValue = TypedValue()
    val isSuccess = theme.resolveAttribute(id, typedValue, true)
    return if (isSuccess) typedValue.data else null
}

//fun <T> Flow<T>.throttleFirst(time: Long): Flow<T> {
//    val lastTime = AtomicLong(0)
//    return filter {
//        val cTime = System.currentTimeMillis()
//        if (cTime - lastTime.get() >= time) {
//            lastTime.set(cTime)
//            true
//        } else {
//            false
//        }
//    }
//}
//
//fun View.setOnThrottledClickListener(duration: Long, scope: LifecycleCoroutineScope, func: () -> Unit) {
//    val view = this
//    val clickFLow = callbackFlow {
//        view.setOnClickListener { trySend(Unit).isSuccess }
//        awaitClose { view.setOnClickListener(null) }
//    }
//    scope.launch {
//        clickFLow.throttleFirst(duration).collect { func.invoke() }
//    }
//}

//fun Context.sp2Px(sp: Int): Float {
//    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics)
//}

fun Context.dp2Px(dp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
}

