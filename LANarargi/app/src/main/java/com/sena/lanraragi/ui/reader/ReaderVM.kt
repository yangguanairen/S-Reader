package com.sena.lanraragi.ui.reader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.NewHttpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * FileName: ReaderVM
 * Author: JiaoCan
 * Date: 2024/3/28
 */

class ReaderVM : ViewModel() {

    val fileNameList = MutableLiveData<List<String>>()

    val curPos = MutableLiveData<Int>()
    var fromWebtoon: Boolean = false


    suspend fun initData(arcId: String) {
        val result = withContext(Dispatchers.IO) {
            NewHttpHelper.extractManga(arcId)
        }
        result?.let {
            fileNameList.value = it
        }
    }

    fun setCurPosition(page: Int) {
        // 校验page正确性
        val totalCount = fileNameList.value?.size ?: 0
        if (page < 0) {
            // TODO: 暂且会出现读取icon的error，虽然不会闪退
//            Toast.makeText(Application.getContext(), "已经没有前一页了哦", Toast.LENGTH_SHORT).show()
            DebugLog.e("ReaderActivity: 错误的当前位置: $page")
            return
        } else if (page > totalCount - 1) {
//            Toast.makeText(Application.getContext(), "已经没有后一页了哦", Toast.LENGTH_SHORT).show()
            DebugLog.e("ReaderActivity: 错误的当前位置: $page")
            return
        }
        curPos.value = page
    }

    fun setFileNameList(list: List<String>) {
        fileNameList.value = list
    }

    fun updateList() {
        val cPos = curPos.value ?: 0
        curPos.value = cPos
    }



}

