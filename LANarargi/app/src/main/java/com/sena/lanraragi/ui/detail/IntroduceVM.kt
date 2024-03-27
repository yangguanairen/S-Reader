package com.sena.lanraragi.ui.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.utils.HttpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * FileName: IntroduceVM
 * Author: JiaoCan
 * Date: 2024/3/26
 */

class IntroduceVM : ViewModel() {


    val archive = MutableLiveData<Archive>()


    suspend fun initData(arcId: String) {
        val result = withContext(Dispatchers.IO) {
            LanraragiDB.findArchiveByArcid(arcId)
        }
        if (result != null) {
            archive.value = result!!
        }

    }

}

