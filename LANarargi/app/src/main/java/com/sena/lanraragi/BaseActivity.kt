package com.sena.lanraragi

import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sena.lanraragi.R
import com.sena.lanraragi.utils.LanguageHelper


/**
 * FileName: BaseActivity
 * Author: JiaoCan
 * Date: 2024/3/25
 */

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.LAnraragiBlack)
        LanguageHelper.getAttachBaseContext(this)
        window.statusBarColor = Color.BLACK
    }

}

