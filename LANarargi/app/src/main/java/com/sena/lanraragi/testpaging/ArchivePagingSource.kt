package com.sena.lanraragi.testpaging

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min


/**
 * FileName: ArchivePagingSource
 * Author: JiaoCan
 * Date: 2024/3/25
 */


private const val STARTING_KEY = 0
private const val LOAD_DELAY_MILLIS = 3_000L

class ArchivePagingSource(
    private val order: LanraragiDB.DBHelper.ORDER, private val sort: LanraragiDB.DBHelper.SORT
) : PagingSource<Int, Archive>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Archive> {


        val allData = LanraragiDB.filterArchiveList(order, sort)

        val start = params.key ?: STARTING_KEY
        println("加载多量数据key: ${params.key}")
        val end = min(start + params.loadSize, allData.size)

        println("加载多量数据: $start")

        val data = allData.subList(start, end)
        // 确保加载的上一页的起始点不会在第0个Item前面
        val preKey = when (start) {
            STARTING_KEY -> null
            else -> ensureValidKey(start - params.loadSize)
        }
        val nextKey = when (end) {
            allData.size -> null
            else -> max(end + 1, allData.size)
        }

        return LoadResult.Page(data, preKey, nextKey)
    }

    override fun getRefreshKey(state: PagingState<Int, Archive>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val articleSource = state.closestPageToPosition(anchorPosition) ?: return null

//        return ensureValidKey((articleSource.nextKey ?: 0) - state.config.pageSize - 1 - (state.config.pageSize / 2))
        return null
    }


    private fun ensureValidKey(key: Int) = max(STARTING_KEY, key)
}

