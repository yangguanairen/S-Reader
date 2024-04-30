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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainVM : ViewModel() {

    val dataList = MutableLiveData<List<Archive>>()
    val filterSort = MutableLiveData<LanraragiDB.DBHelper.SORT>()
    val filterOrder = MutableLiveData<LanraragiDB.DBHelper.ORDER>()
    val queryText = MutableLiveData<String>()
    val isNew = MutableLiveData<Boolean>()

    val categories = MutableLiveData<List<Category>>()
    val curCategory = MutableLiveData<Category?>()

    // forceRefreshData()作为App整个数据的基本来源
    // 不参与管理
    private var lastJob: Job? = null
    private var lastInitJob: Job? = null
    private var isInitialized: Boolean = false

    fun refreshData(isForce: Boolean) {
        if (isInitialized && !isForce) return
        isInitialized = true
        lastJob?.cancel()
        lastInitJob?.cancel()
        val newJob = viewModelScope.launch {
            dataList.value = withContext(Dispatchers.IO) {
                NewHttpHelper.queryAllArchive()
                NewHttpHelper.queryArchiveByTag("")
            }
            refreshTagsData()
            refreshCategoriesData()
        }
        lastInitJob = newJob
    }

    private suspend fun refreshTagsData() {
        val result  = withContext(Dispatchers.IO) {
            NewHttpHelper.getAllTags()
        }
        if (result == null) {
            DebugLog.e("List为空, 不更新Stats表")
        } else {
            LanraragiDB.updateStatsTable(result)
        }
    }

    private suspend fun refreshCategoriesData() {
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

    fun setQueryText(query: String) {
        queryText.value  = query
        curCategory.value = null
        mainVMInvoke("setQueryText() $query") {
            val result = NewHttpHelper.queryArchiveByTag(query)
            dataList.value = result
        }
    }

    fun setSort(sort: LanraragiDB.DBHelper.SORT) {
        if (filterSort.value == sort) return
        filterSort.value = sort
        AppConfig.sort = sort
        val query = queryText.value ?: ""

        mainVMInvoke("setSort(): ${sort.name}") {
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
        if (filterOrder.value == order) return
        filterOrder.value = order
        AppConfig.order = order
        val query = queryText.value ?: ""

        mainVMInvoke("setOrder(): ${order.name}") {
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

        mainVMInvoke("setNewState(): $b") {
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

    fun setCategory(category: Category?) {
        if (category == null) {
            curCategory.value = null
            setQueryText("")
        } else {
            mainVMInvoke("setCategory(): \n$category") {
                val result = withContext(Dispatchers.IO) {
                    LanraragiDB.queryArchivesByIdList(category.archives)
                }
                dataList.value = result
                curCategory.value = category
                queryText.value = ""
            }
        }
    }

    suspend fun getSingleRandomArchive(): Archive? {
        val result = NewHttpHelper.getRandomArchive(1)
        return result.getOrNull(0)
    }


    private fun mainVMInvoke(log: String? = null, func: suspend () -> Unit) {
        val lastJobIsFinish = lastJob?.isCompleted
        if (lastJobIsFinish == true) {
            DebugLog.i("MainVM: $log 上一个任务已经完成")
        } else {
            lastJob?.cancel()
            val lastCancelResult = lastJob?.isCancelled
            DebugLog.i("MainVM: Log: $log 取消结果: $lastCancelResult")
        }

        val newJob = viewModelScope.launch {
            func.invoke()
        }
        lastJob = newJob
    }

}