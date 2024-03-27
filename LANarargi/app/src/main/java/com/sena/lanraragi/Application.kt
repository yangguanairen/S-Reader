package com.sena.lanraragi

import android.app.Application
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.utils.LanguageHelper


/**
 * FileName: Application
 * Author: JiaoCan
 * Date: 2024/3/25
 */

class Application : Application() {


    override fun onCreate() {
        super.onCreate()

        LanraragiDB.init(this)

    }
}

