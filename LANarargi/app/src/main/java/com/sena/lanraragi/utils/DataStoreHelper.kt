package com.sena.lanraragi.utils

import android.content.Context
import com.sena.lanraragi.database.LanraragiDB


/**
 * FileName: DataStoreHelper
 * Author: JiaoCan
 * Date: 2024/4/16
 */

object DataStoreHelper {


    enum class KEY(val s: String) {
        SERVER_HOST("server_host"),
        SERVER_SECRET_KEY("server_secret_Key"),

        /*
        COMMON_SCROLL_REFRESH("common_scroll_refresh"),
         */
        COMMON_THEME("common_theme"),
        COMMON_LANGUAGE("common_language"),
        COMMON_VIEW_METHOD("common_view_method"),

        READ_RTL("read_rtl"),
        READ_VOICE("read_voice"),
        /*
        READ_MERGE("read_merge"),
        READ_REVERSE_MERGE("read_reverse_merge"),
        READ_MERGE_METHOD("read_merge_method"),
         */
        READ_SCALE_METHOD("read_scale_method"),
        READ_KEEP_SCREEN_LIGHT("read_keep_screen_light"),
        READ_SYN_PROGRESS("read_syn_progress"),

        SEARCH_LOCAL("search_local"),
        SEARCH_DELAY("search_delay"),

        RANDOM_COUNT("random_count"),

        DEBUG_DETAIL("debug_detail"),
        DEBUG_CRASH("debug_crash"),

        FILTER_NEW("filter_new"),
        FILTER_SORT("filter_sort"),
        FILTER_ORDER("filter_order")
    }

    private const val spName = "lanraragi_sp"




    fun <T> getValue(context: Context, key: KEY, defValue: T): T {
        val sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE) ?: return defValue
        val result: Any? = when (defValue) {
            is String -> sp.getString(key.s, defValue)
            is Int -> sp.getInt(key.s, defValue)
            is Float -> sp.getFloat(key.s, defValue)
            is Boolean -> sp.getBoolean(key.s, defValue)
            is Long -> sp.getLong(key.s, defValue)
            is AppTheme -> when (sp.getString(key.s, null)) {
                AppTheme.Dark.name -> AppTheme.Dark
                AppTheme.HVerse.name -> AppTheme.HVerse
                else -> defValue
            }
            is AppLanguage -> when (sp.getString(key.s, null)) {
                AppLanguage.CHINA.name -> AppLanguage.CHINA
                AppLanguage.JAPAN.name -> AppLanguage.JAPAN
                AppLanguage.ENGLISH.name -> AppLanguage.ENGLISH
                else -> defValue
            }
            is CardType -> when (sp.getString(key.s, null)) {
                CardType.VERTICAL.name -> CardType.VERTICAL
                CardType.LAND.name -> CardType.LAND
                else -> defValue
            }
            is LanraragiDB.DBHelper.SORT, -> when (sp.getString(key.s, null)) {
                LanraragiDB.DBHelper.SORT.TIME.name -> LanraragiDB.DBHelper.SORT.TIME
                LanraragiDB.DBHelper.SORT.TITLE.name -> LanraragiDB.DBHelper.SORT.TITLE
                else -> defValue
            }
            is LanraragiDB.DBHelper.ORDER, -> when (sp.getString(key.s, null)) {
                LanraragiDB.DBHelper.ORDER.ASC.name -> LanraragiDB.DBHelper.ORDER.ASC
                LanraragiDB.DBHelper.ORDER.DESC.name -> LanraragiDB.DBHelper.ORDER.DESC
                else -> defValue
            }
            is ScaleType -> when (sp.getString(key.s, null)) {
                ScaleType.FIT_WIDTH.name -> ScaleType.FIT_WIDTH
                ScaleType.FIT_HEIGHT.name -> ScaleType.FIT_HEIGHT
                ScaleType.FIT_PAGE.name -> ScaleType.FIT_PAGE
                ScaleType.WEBTOON.name -> ScaleType.WEBTOON
                else -> defValue
            }
            else -> {
                var type = "null"
                defValue?.let {
                    type = it::class.java.name
                }
                DebugLog.e("getValue() 无法识别的类型: $type, key: ${key.s}")
                defValue
            }
        }
        return getOrNull { result as T } ?: defValue

    }

    fun <T> updateValue(context: Context, key: KEY, value: T) {
        val sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE) ?: return
        when (value) {
            is String -> sp.edit().putString(key.s, value).apply()
            is Int -> sp.edit().putInt(key.s, value).apply()
            is Float -> sp.edit().putFloat(key.s, value).apply()
            is Boolean -> sp.edit().putBoolean(key.s, value).apply()
            is Long -> sp.edit().putLong(key.s, value).apply()
            is AppTheme -> sp.edit().putString(key.s, value.name).apply()
            is AppLanguage -> sp.edit().putString(key.s, value.name).apply()
            is CardType -> sp.edit().putString(key.s, value.name).apply()
            is LanraragiDB.DBHelper.SORT -> sp.edit().putString(key.s, value.name).apply()
            is LanraragiDB.DBHelper.ORDER -> sp.edit().putString(key.s, value.name).apply()
            is ScaleType -> sp.edit().putString(key.s, value.name).apply()
            else -> {
                var type = "null"
                value?.let {
                    type = it::class.java.name
                }
                DebugLog.e("updateValue() 无法识别的类型: $type, key: ${key.s}")
            }
        }
    }

}

