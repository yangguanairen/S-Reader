package com.sena.lanraragi

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sena.lanraragi.utils.LanguageHelper
import kotlinx.coroutines.launch


/**
 * FileName: BaseActivity
 * Author: JiaoCan
 * Date: 2024/3/25
 */

abstract class BaseActivity(@MenuRes menuId: Int? = null) : AppCompatActivity() {

    private var mToolbar: Toolbar? = null
    private val mMenuId = menuId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        LanguageHelper.getAttachBaseContext(this)
        window.statusBarColor = Color.BLACK
    }

    override fun onStart() {
        super.onStart()
        mToolbar = findViewById(R.id.toolbar)
        mToolbar?.let { initAppbar(it) }
    }

    private fun initAppbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    protected fun setAppBarText(title: String?, subtitle: String?) {
        // mToolbar的获取在onCreate()中执行
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                supportActionBar?.run {
                    this.title = title
                    this.subtitle = subtitle
                }
                // 切换文本动画
                mToolbar?.apply { layoutTransition = LayoutTransition() }
            }
        }

    }

    protected fun setNavigation(@DrawableRes id: Int, func: () -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                supportActionBar?.apply {
                    setHomeAsUpIndicator(id)
                }
                mToolbar?.let { toolbar ->
                    toolbar.setNavigationOnClickListener { func.invoke() }
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (mMenuId != null) {
            menuInflater.inflate(mMenuId, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }


    /**
     * 点击空白收起软键盘
     * https://blog.csdn.net/qq_36347817/article/details/89838845
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v?.windowToken)
            }

        }

        return super.dispatchTouchEvent(ev)
    }


    private fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if (v == null || v !is EditText) return false
        val location = intArrayOf(0, 0)
        v.getLocationInWindow(location)
        val left = location[0]
        val top = location[1]
        val right = left + v.width
        val bottom = top + v.height
        return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
    }

    private fun hideKeyboard(token: IBinder?) {
        if (token == null) return
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
    }

}

