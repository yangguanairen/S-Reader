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

    val archive = MutableLiveData<Archive>()
    val pages = MutableLiveData<List<String>>()


    suspend fun initData(arcId: String) {

        val result = withContext(Dispatchers.IO) {
            LanraragiDB.findArchiveByArcid(arcId)
        }
        result?.let { archive.value = it }
        val tt = withContext(Dispatchers.IO) {
            HttpHelper.getAllPageName(arcId)
        }
        tt?.let { pages.value = it }
    }
}

