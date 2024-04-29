package com.sena.lanraragi.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.database.category.Category
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.NewHttpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainVM : ViewModel() {

    val serverArchiveCount = MutableLiveData<Int>()
    val dataList = MutableLiveData<List<Archive>>()
    val filterSort = MutableLiveData<LanraragiDB.DBHelper.SORT>()
    val filterOrder = MutableLiveData<LanraragiDB.DBHelper.ORDER>()
    val queryText = MutableLiveData<String>()
    val isNew = MutableLiveData<Boolean>()

    val categories = MutableLiveData<List<Category>>()
    val curCategory = MutableLiveData<Category?>()

    fun forceRefreshData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                NewHttpHelper.queryAllArchive()
            }
            val text = queryText.value ?: ""
            val result = withContext(Dispatchers.IO) {
                NewHttpHelper.queryArchiveByTag(text)
            }
            dataList.value = result
            if (serverArchiveCount.value != result.size) {
                serverArchiveCount.value = result.size
            }
        }
    }

    fun refreshTagsData() {
        viewModelScope.launch {
            val result  = withContext(Dispatchers.IO) {
                NewHttpHelper.getAllTags()
            }
            if (result == null) {
                DebugLog.e("List为空, 不更新Stats表")
            } else {
                LanraragiDB.updateStatsTable(result)
            }
        }
    }

    fun refreshCategoriesData() {
        viewModelScope.launch {
            val result  = withContext(Dispatchers.IO) {
                NewHttpHelper.getAllCategories()
            }
            if (result == null) {
                DebugLog.e("List为空, 不更新Stats表")
            } else {
                LanraragiDB.updateCategoryTable(result)
            }
            val dbResult = withContext(Dispatchers.IO) {
                LanraragiDB.queryAllCategories()
            }
            categories.value = dbResult
        }
    }

    fun setQueryText(query: String) {
        viewModelScope.launch {
            queryText.value  = query
            curCategory.value = null
            val result = NewHttpHelper.queryArchiveByTag(query)
            dataList.value = result
            if (serverArchiveCount.value != result.size) {
                serverArchiveCount.value = result.size
            }
        }
    }

    fun setSort(sort: LanraragiDB.DBHelper.SORT) {
        filterSort.value = sort
        AppConfig.sort = sort
        val query = queryText.value ?: ""

        viewModelScope.launch {
            val cCategory = curCategory.value
            if (cCategory != null) {
                setCategory(cCategory)
            } else {
                val result = withContext(Dispatchers.IO) {
                    LanraragiDB.queryArchivesWithTag(query)
                }
                dataList.value = result
            }
        }
    }

    fun setOrder(order: LanraragiDB.DBHelper.ORDER) {
        filterOrder.value = order
        AppConfig.order = order
        val query = queryText.value ?: ""

        viewModelScope.launch {
            val cCategory = curCategory.value
            if (cCategory != null) {
                setCategory(cCategory)
            } else {
                val result = withContext(Dispatchers.IO) {
                    LanraragiDB.queryArchivesWithTag(query)
                }
                dataList.value = result
            }
        }
    }

    fun setNewState(b: Boolean) {
        isNew.value = b
        AppConfig.isNew = b
        val query = queryText.value ?: ""

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                LanraragiDB.queryArchivesWithTag(query)
            }
            dataList.value = result
        }
    }

    fun setCategory(category: Category?) {
        if (category == null) {
            curCategory.value = null
            setQueryText("")
        } else {
            viewModelScope.launch {
                val result = withContext(Dispatchers.IO) {
                    LanraragiDB.queryArchivesByIdList(category.archives)
                }

                dataList.value = result
                curCategory.value = category
                queryText.value = ""
                if (serverArchiveCount.value != result.size) {
                    serverArchiveCount.value = result.size
                }
            }
        }
    }

    suspend fun getSingleRandomArchive(): Archive? {
        val result = NewHttpHelper.getRandomArchive(1)
        return result.getOrNull(0)
    }

}