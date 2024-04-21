package com.sena.lanraragi

import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.utils.ScaleType


/**
 * FileName: AppConfig
 * Author: JiaoCan
 * Date: 2024/4/16
 */

object AppConfig {

    // 服务器
    var serverHost: String = ""
    var serverSecretKey: String = ""

    // 通用
    var enableScrollRefresh: Boolean = false
    var theme: String = ""
    var viewMethod: String = ""

    // 阅读
    var enableRtl: Boolean = false   // 是否启用从右向左阅读
    var enableVoice: Boolean = false // 是否启动音量键翻页
    var enableMerge: Boolean = false
    var enableReverseMerge: Boolean = false
    var mergeMethod: String = ""
    var scaleMethod: ScaleType = ScaleType.FIT_WIDTH
    var screenOvertime: Int = 5

    // 搜索
    var enableLocalSearch: Boolean = false  // 是否启用本地搜索
    var searchDelay: Int = 750              // 两次搜索的间隔

    // 随机档案
    var randomCount: Int = 1

    // 调试
    var enableShowDetail: Boolean = false   // 是否展示详细的错误信息
    var enableCrashInfo: Boolean = false    // 是否展示崩溃信息

    // 筛选
    var isNew: Boolean = false
    var sort: LanraragiDB.DBHelper.SORT = LanraragiDB.DBHelper.SORT.TIME
    var order: LanraragiDB.DBHelper.ORDER = LanraragiDB.DBHelper.ORDER.DESC
}

