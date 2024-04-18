package com.sena.lanraragi

import android.app.Application
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.utils.DataStoreHelper


/**
 * FileName: Application
 * Author: JiaoCan
 * Date: 2024/3/25
 */

class Application : Application() {


    override fun onCreate() {
        super.onCreate()

        LanraragiDB.init(this)

        initAppConfig()

    }

    private fun initAppConfig() {
        val context = this
        AppConfig.apply {
            serverHost = DataStoreHelper.getValue(context, DataStoreHelper.KEY.SERVER_HOST, "")
            serverSecretKey = DataStoreHelper.getValue(context, DataStoreHelper.KEY.SERVER_SECRET_KEY, "")

            enableScrollRefresh = DataStoreHelper.getValue(context, DataStoreHelper.KEY.COMMON_SCROLL_REFRESH, false)
            theme = DataStoreHelper.getValue(context, DataStoreHelper.KEY.COMMON_THEME, getString(R.string.setting_common_apptheme_select_1))
            viewMethod = DataStoreHelper.getValue(context, DataStoreHelper.KEY.COMMON_VIEW_METHOD, getString(R.string.setting_common_view_method_select_1))
            enableRtl = DataStoreHelper.getValue(context, DataStoreHelper.KEY.READ_RTL, false)
            enableVoice = DataStoreHelper.getValue(context, DataStoreHelper.KEY.READ_VOICE, false)
            enableMerge = DataStoreHelper.getValue(context, DataStoreHelper.KEY.READ_MERGE, false)
            enableReverseMerge = DataStoreHelper.getValue(context, DataStoreHelper.KEY.READ_REVERSE_MERGE, false)
            mergeMethod = DataStoreHelper.getValue(context, DataStoreHelper.KEY.READ_MERGE_METHOD, getString(R.string.setting_read_merge_method_select_1))
            scaleMethod = DataStoreHelper.getValue(context, DataStoreHelper.KEY.READ_SCALE_METHOD, getString(R.string.setting_read_scale_method_select_1))
            screenOvertime = DataStoreHelper.getValue(context, DataStoreHelper.KEY.READ_SCREEN_OVER_TIME, 5)

            enableLocalSearch = DataStoreHelper.getValue(context, DataStoreHelper.KEY.SEARCH_LOCAL, false)
            searchDelay = DataStoreHelper.getValue(context, DataStoreHelper.KEY.SEARCH_DELAY, 750)

            randomCount = DataStoreHelper.getValue(context, DataStoreHelper.KEY.RANDOM_COUNT, 1)

            enableShowDetail = DataStoreHelper.getValue(context, DataStoreHelper.KEY.DEBUG_DETAIL, false)
            enableCrashInfo = DataStoreHelper.getValue(context, DataStoreHelper.KEY.DEBUG_CRASH, false)

            isNew = DataStoreHelper.getValue(context, DataStoreHelper.KEY.FILTER_NEW, false)
            sort = DataStoreHelper.getValue(context, DataStoreHelper.KEY.FILTER_SORT, LanraragiDB.DBHelper.SORT.TIME)
            order = DataStoreHelper.getValue(context, DataStoreHelper.KEY.FILTER_ORDER, LanraragiDB.DBHelper.ORDER.DESC)

            // 测试使用
            serverHost = "http://192.168.0.102:3002"
        }
    }
}

