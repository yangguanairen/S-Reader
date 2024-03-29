package com.sena.lanraragi.ui.reader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.utils.HttpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * FileName: ReaderVM
 * Author: JiaoCan
 * Date: 2024/3/28
 */

class ReaderVM : ViewModel() {

    val fileNameList = MutableLiveData<List<String>>()

    // 除了ViewPager2以外的位置改变
    val curPos1 = MutableLiveData<Int>()
    // ViewPager2发生位置改变
    val curPos2 = MutableLiveData<Int>()


    suspend fun initData(arcId: String) {
        val result = withContext(Dispatchers.IO) {
            HttpHelper.getAllPageName(arcId)
        }
        result?.let { fileNameList.value = it }
    }

    fun setCurPosition1(p: Int) {
        curPos1.value = p
    }

    fun setCurPosition2(p: Int) {
        curPos2.value = p
    }

    fun setFileNameList(list: List<String>) {
        fileNameList.value = list
    }



}

