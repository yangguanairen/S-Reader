package com.sena.lanraragi

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.testpaging.ArchiveRepository
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.OkHttpHelper
import kotlinx.coroutines.flow.Flow


/**
 * FileName: ArchiveListVM
 * Author: JiaoCan
 * Date: 2024/3/22
 */

private const val ITEMS_PER_PAGE = 20

class MainVM(private val repository: ArchiveRepository) : ViewModel() {


    var items: Flow<PagingData<Archive>> = createPager().flow.cachedIn(viewModelScope)


    val filterSort = MutableLiveData<LanraragiDB.DBHelper.SORT>()
    val filterOrder = MutableLiveData<LanraragiDB.DBHelper.ORDER>()


    // 在页面展示前初始化数据库数据
    suspend fun initData() {
        val allArchives = OkHttpHelper.requestAllArchive()
        if (allArchives == null) {
            DebugLog.e("请求失败，不更新数据库")
            return
        }
        LanraragiDB.updateArchiveList(allArchives)
    }

    fun setSort(sort: LanraragiDB.DBHelper.SORT) {
        filterSort.value = sort
        items = createPager().flow.cachedIn(viewModelScope)
    }

    fun setOrder(order: LanraragiDB.DBHelper.ORDER) {
        filterOrder.value = order
        items = createPager().flow.cachedIn(viewModelScope)
    }

    private fun createPager() = Pager(
        PagingConfig(ITEMS_PER_PAGE, enablePlaceholders = false),
        pagingSourceFactory = { repository.archivePagingSource(
            filterOrder.value ?: LanraragiDB.DBHelper.ORDER.TIME,
            filterSort.value ?: LanraragiDB.DBHelper.SORT.DESC
        ) }
    )
}

