package com.sena.lanraragi.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.HttpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainVM() : ViewModel() {

    val serverArchiveCount = MutableLiveData<Int>()
    val dataList = MutableLiveData<List<Archive>>()
    val filterSort = MutableLiveData<LanraragiDB.DBHelper.SORT>()
    val filterOrder = MutableLiveData<LanraragiDB.DBHelper.ORDER>()


    // 在页面展示前初始化数据库数据
    suspend fun initData() {
        val allArchives = HttpHelper.requestAllArchive()
        if (allArchives == null) {
            DebugLog.e("请求失败，不更新数据库")
            return
        }
        serverArchiveCount.value = allArchives.size
        LanraragiDB.updateArchiveList(allArchives)
    }

    suspend fun setSort(sort: LanraragiDB.DBHelper.SORT) {
        filterSort.value = sort
        val result = LanraragiDB.DBHelper.filterArchiveList(sort, filterOrder.value ?: LanraragiDB.DBHelper.ORDER.DESC)
        DebugLog.d("测试: setSort(): ${result.size}")
//        DebugLog.d("测试: setSort() 详细数据: ${result.subList(0, 5).joinToString("\n")}")
        dataList.value = result
    }

    suspend fun setOrder(order: LanraragiDB.DBHelper.ORDER) {
        filterOrder.value = order
        val result = LanraragiDB.DBHelper.filterArchiveList(filterSort.value ?: LanraragiDB.DBHelper.SORT.TITLE, order)
        DebugLog.d("测试: setOrder(): ${result.size}")

        dataList.value = result
    }

    suspend fun queryFromServer(query: String) {
        val order = filterOrder.value ?: LanraragiDB.DBHelper.ORDER.DESC
        val sort = filterSort.value ?: LanraragiDB.DBHelper.SORT.TIME
        val result = withContext(Dispatchers.IO) {
            HttpHelper.search(query, order, sort) ?: emptyList()
        }
        DebugLog.d("测试: queryFromServer(): ${result.size}")
        DebugLog.d(result.joinToString("\n"))

        dataList.value = result
    }

    suspend fun queryFromDB(query: String) {
        val order = filterOrder.value ?: LanraragiDB.DBHelper.ORDER.DESC
        val sort = filterSort.value ?: LanraragiDB.DBHelper.SORT.TIME

        if (query.isBlank()) {
            val result = withContext(Dispatchers.IO) {
                LanraragiDB.DBHelper.filterArchiveList(sort, order)
            }
            DebugLog.d("测试: queryFromDB(): ${result.size}")

            dataList.value = result
        }
    }

}