package com.sena.lanraragi.utils

import android.util.Log
import android.widget.Toast
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.LanraragiApplication


/**
 * FileName: DebugLOg
 * Author: JiaoCan
 * Date: 2024/3/22
 */


object DebugLog {
    const val VERBOSE = 5
    const val DEBUG = 4
    const val INFORMATION = 3
    const val WARNING = 2
    const val ERROR = 1
    const val PRODUCTION = 0
    private var TAG = "SDKTEST"
    private var DEBUG_LEVEL = VERBOSE
    private const val MAX_SIZE = 2048
    fun setTag(tag: String) {
        TAG = tag
    }

    fun setDebugLevel(debugLevel: Int) {
        DEBUG_LEVEL = debugLevel
    }

    fun v(toPrint: Any) {
        if (DEBUG_LEVEL < DEBUG) return
        printString(toPrint.toString(), object : Print {
            override fun print(tag: String, s: String) {
                Log.v(tag, s)
            }
        })
    }

    fun d(toPrint: Any) {
        if (DEBUG_LEVEL < DEBUG) return
        printString(toPrint.toString(), object : Print {
            override fun print(tag: String, s: String) {
                Log.d(tag, s)
            }
        })
    }

    fun i(toPrint: Any) {
        if (DEBUG_LEVEL < DEBUG) return
        printString(toPrint.toString(), object : Print {
            override fun print(tag: String, s: String) {
                Log.i(tag, s)
            }
        })
    }

    fun w(toPrint: Any) {
        if (DEBUG_LEVEL < DEBUG) return
        printString(toPrint.toString(), object : Print {
            override fun print(tag: String, s: String) {
                Log.w(tag, s)
            }
        })
    }

    fun e(toPrint: Any) {
        if (DEBUG_LEVEL < DEBUG) return
        printString(toPrint.toString(), object : Print {
            override fun print(tag: String, s: String) {
                Log.e(tag, s)
            }
        })
        if (AppConfig.enableShowDetail) {
            Toast.makeText(LanraragiApplication.getContext(), toPrint.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     *
     * @param s  //待输出的字符串
     * @param print  //以何种方式输出，例如Log.d(TAG, s)
     */
    private fun printString(os: String, print: Print) {
        var s = os
        while (s.length > MAX_SIZE) {
            print.print(TAG, s.substring(0, MAX_SIZE))
            s = s.substring(MAX_SIZE, s.length)
        }
        print.print(TAG, s)
    }

    // 函数式接口，有且仅有一个函数，可定义变量
    private interface Print {
        fun print(tag: String, s: String)
    }
}
