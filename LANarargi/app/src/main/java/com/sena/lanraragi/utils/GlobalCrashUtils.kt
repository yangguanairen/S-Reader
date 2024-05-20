package com.sena.lanraragi.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.LanraragiApplication
import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat


/**
 * FileName: GlobalCrashUtils
 * Author: JiaoCan
 * Date: 2024/4/26
 */

object GlobalCrashUtils {

    private var mainCrashHandler: MainCrashHandler? = null
    private var uncaughtCrashHandler: UncaughtCrashHandler? = null
    private const val crashFileName = "crashLog.txt"


    fun init(context: Context): GlobalCrashUtils {
        setMainCrashHandler { _, e ->
            handlerCrash(context, e)
        }
        setUncaughtCrashHandler { _, e ->
            handlerCrash(context, e)
        }
        return this
    }

    fun register(application: LanraragiApplication) {
        Handler(Looper.getMainLooper()).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    toastInDebugMode(e, true)
                    mainCrashHandler?.mainException(Looper.getMainLooper().thread, e)
                }
            }
        }
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            toastInDebugMode(e, false)
            uncaughtCrashHandler?.uncaughtException(t, e)
        }
    }

    fun getCrashText(context: Context): String {
        val file = File(context.externalCacheDir, crashFileName)
        return file.readText()
    }


    private fun handlerCrash(context: Context, crash: Throwable) {
        val sb = StringBuilder()
        @SuppressLint("SimpleDateFormat")
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val time = format.format(System.currentTimeMillis())
        sb.appendLine(time)
        sb.appendLine(crash.localizedMessage)
        crash.stackTrace.forEach {
            sb.appendLine(it.toString())
        }
        val crashStr = sb.toString()
        val file = File(context.externalCacheDir, crashFileName)
        file.appendText(crashStr, Charset.forName("UTF-8"))
    }

    private fun toastInDebugMode(e: Throwable, isMain: Boolean) {
        DebugLog.e("未捕获的线程异常行为: ${e.stackTraceToString()}")
        e.printStackTrace()
        if (AppConfig.enableCrashInfo) {
            Handler(Looper.getMainLooper()).post {
                val showText = "Crash Problem in ${if (isMain) "Main" else "Other"}Thread" + e.stackTraceToString()
                toast(showText)
            }
        }
    }

    fun setMainCrashHandler(func: ((t: Thread, e: Throwable) -> Unit)?): GlobalCrashUtils {
        mainCrashHandler = if (func == null) {
            null
        } else {
            object : MainCrashHandler {
                override fun mainException(t: Thread, e: Throwable) {
                    func.invoke(t, e)
                }
            }
        }
        return this
    }

    fun setUncaughtCrashHandler(func: ((t: Thread, e: Throwable) -> Unit)?): GlobalCrashUtils {
        uncaughtCrashHandler = if (func == null) {
            null
        } else {
            object : UncaughtCrashHandler {
                override fun uncaughtException(t: Thread, e: Throwable) {
                    func.invoke(t, e)
                }
            }
        }
        return this
    }

    private interface MainCrashHandler {
        fun mainException(t: Thread, e: Throwable)
    }

    private interface UncaughtCrashHandler {
        fun uncaughtException(t: Thread, e: Throwable)
    }
}

