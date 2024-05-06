package com.sena.lanraragi.ui.reader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.lanraragi.R
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.NewHttpHelper
import com.sena.lanraragi.utils.PosSource
import com.sena.lanraragi.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * FileName: ReaderVM
 * Author: JiaoCan
 * Date: 2024/3/28
 */

class ReaderVM : ViewModel() {

    val fileNameList = MutableLiveData<List<String>>()

    val curPos = MutableLiveData<Pair<Int, PosSource>>()


    suspend fun initData(arcId: String) {
        val result = withContext(Dispatchers.IO) {
            NewHttpHelper.extractManga(arcId)
        }
        result?.let {
            fileNameList.value = it
        }
        result?:let {
            val size = fileNameList.value?.size ?: 0
            val errorList = (0 until size).map { "error" }
            fileNameList.value = errorList
        }
    }

    fun setCurPosition(page: Int, source: PosSource) {
        if (source == PosSource.PreviewFragment) {
            curPos.value = Pair(page, source)
            return
        }
        // 校验page正确性
        val totalCount = fileNameList.value?.size ?: 0
        if (page < 0) {
            toast(R.string.read_no_pre_page)
            DebugLog.d("ReaderActivity: 错误的当前位置: $page")
            return
        } else if (page > totalCount - 1) {
            toast(R.string.read_no_next_page)
            DebugLog.d("ReaderActivity: 错误的当前位置: $page")
            return
        }
        curPos.value = Pair(page, source)
    }

    fun upPage() {
        val pos = curPos.value?.first ?: 0
        setCurPosition(pos + 1, PosSource.Other)
    }

    fun downPage() {
        val pos = curPos.value?.first ?: 0
        setCurPosition(pos - 1, PosSource.Other)
    }

    fun setFileNameList(list: List<String>) {
        fileNameList.value = list
    }

    fun updateList(source: PosSource) {
        val cPos: Int = curPos.value?.first ?: 0
        curPos.value = Pair(cPos, source)
    }

    fun updateThumb(id: String) {
        val page = (curPos.value?.first ?: 0) + 1
        viewModelScope.launch {
            val isSuccess = withContext(Dispatchers.IO) {
                NewHttpHelper.updateServerThumb(id, page)
            }
            if (isSuccess) {
                toast(R.string.read_set_thumb_success, page)
            } else {
                toast(R.string.read_set_thumb_failed)
            }
        }
    }


}

