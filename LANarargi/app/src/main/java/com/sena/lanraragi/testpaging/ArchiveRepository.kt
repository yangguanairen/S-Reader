package com.sena.lanraragi.testpaging

import com.sena.lanraragi.database.LanraragiDB


/**
 * FileName: ArchiveRespository
 * Author: JiaoCan
 * Date: 2024/3/25
 */

class ArchiveRepository {

    fun archivePagingSource(order: LanraragiDB.DBHelper.ORDER, sort: LanraragiDB.DBHelper.SORT) = ArchivePagingSource(order, sort)



}

