package com.sena.lanraragi

import android.app.Application
import android.content.Context
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.utils.DataStoreHelper
import com.sena.lanraragi.utils.GlobalCrashUtils
import com.sena.lanraragi.utils.ScaleType


/**
 * FileName: Application
 * Author: JiaoCan
 * Date: 2024/3/25
 */

class LanraragiApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        instance = this

        LanraragiDB.init(this)

        initAppConfig()

        GlobalCrashUtils.init(this)
            .register(this)

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
            scaleMethod = DataStoreHelper.getValue(context, DataStoreHelper.KEY.READ_SCALE_METHOD, ScaleType.FIT_WIDTH)
            enableScreenLight = DataStoreHelper.getValue(context, DataStoreHelper.KEY.READ_KEEP_SCREEN_LIGHT, false)

            enableLocalSearch = DataStoreHelper.getValue(context, DataStoreHelper.KEY.SEARCH_LOCAL, false)
            searchDelay = DataStoreHelper.getValue(context, DataStoreHelper.KEY.SEARCH_DELAY, 750)

            randomCount = DataStoreHelper.getValue(context, DataStoreHelper.KEY.RANDOM_COUNT, 1)

            enableShowDetail = DataStoreHelper.getValue(context, DataStoreHelper.KEY.DEBUG_DETAIL, false)
            enableCrashInfo = DataStoreHelper.getValue(context, DataStoreHelper.KEY.DEBUG_CRASH, false)

            isNew = DataStoreHelper.getValue(context, DataStoreHelper.KEY.FILTER_NEW, false)
            sort = DataStoreHelper.getValue(context, DataStoreHelper.KEY.FILTER_SORT, LanraragiDB.DBHelper.SORT.TIME)
            order = DataStoreHelper.getValue(context, DataStoreHelper.KEY.FILTER_ORDER, LanraragiDB.DBHelper.ORDER.DESC)
        }
    }

    companion object {

         private lateinit var instance: LanraragiApplication

        fun getContext(): Context {
            return instance.applicationContext
        }

    }
}

