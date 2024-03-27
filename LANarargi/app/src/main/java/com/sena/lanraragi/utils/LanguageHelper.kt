package com.sena.lanraragi.utils

import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi
import java.util.Locale


/**
 * FileName: LanguageHelper
 * Author: JiaoCan
 * Date: 2024/3/25
 *
 * https://www.jianshu.com/p/80bf12fd4224
 *
 * https://blog.csdn.net/iblade/article/details/135021407
 */

object LanguageHelper {


    /**
     * 设置应用语言跟随系统
     */
    fun setAppLanguageAuto(context: Context) {
        setAppLanguage(context)
    }

    /**
     * 设置应用语言跟随用户选择
     */
    fun setAppLanguageByLocal(context: Context, code: String) {
        val locale = when (code) {
            Locale.CHINA.language -> Locale.CHINA
            Locale.JAPAN.language -> Locale.JAPAN
            Locale.ENGLISH.language -> Locale.ENGLISH
            else -> Locale.ENGLISH
        }
        setAppLanguage(context, locale)
    }



    fun getAttachBaseContext(context: Context): Context {
        setAppLanguage(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return setAppLanguageApi24(context)
        } else {
            setAppLanguage(context)
        }
        return context
    }

    /**
     * 适配 7.0 以上
     * 实际上并没有用，只能拿返回的context去getString
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setAppLanguageApi24(context: Context): Context {
        val locale = getSystemLocale()
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        return context.createConfigurationContext(configuration)
    }


    private fun setAppLanguage(context: Context, locale: Locale? = null) {
        val mLocale = locale ?: getSystemLocale()
        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(mLocale)
        } else {
            configuration.locale = mLocale
        }
        resources.updateConfiguration(configuration, displayMetrics)
    }


    private fun getSystemLocale(): Locale {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }

        return when (systemLocale.language) {
            Locale.CHINA.language -> Locale.CHINA
            Locale.JAPAN.language -> Locale.JAPAN
            Locale.ENGLISH.language -> Locale.ENGLISH
            else -> Locale.ENGLISH
        }
    }

}

