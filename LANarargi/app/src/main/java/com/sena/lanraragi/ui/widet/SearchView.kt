package com.sena.lanraragi.ui.widet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.databinding.ViewSearchBinding
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.getOrNull


/**
 * FileName: SearchView
 * Author: JiaoCan
 * Date: 2024/3/26
 */

@SuppressLint("ViewConstructor", "InflateParams")
class SearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val mContext = context

    private val binding = ViewSearchBinding.inflate(LayoutInflater.from(mContext), this, true)

    private var mFinishListener: OnInputFinishListener? = null
    private var mClearListener: OnClearTextListener? = null
    private var mDoneListener: OnSearchDoneListener? = null

    private val mWhat = 1
    private var delayTime = AppConfig.searchDelay.toLong()
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val what = msg.what
            if (what != mWhat) return
            val text = getOrNull { msg.obj.toString() }
            DebugLog.d("SearchView 接收: $text")
            if (text == null)  return
            mFinishListener?.onInputFinish(text)
        }
    }

    private val mTextChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.clear.visibility = if (s?.length != 0) View.VISIBLE else View.INVISIBLE
            DebugLog.d("SearchView 输入: $s")
            if (s == null) return

            val message = Message()
            message.what = mWhat
            message.obj = s
            mHandler.removeMessages(mWhat)
            mHandler.sendMessageDelayed(message, delayTime)
        }

        override fun afterTextChanged(s: Editable?) {
        }

    }

    init {
        initView()
    }


    private fun initView() {
        binding.input.addTextChangedListener(mTextChangeListener)

        binding.input.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val queryText = v.editableText?.toString() ?: return@setOnEditorActionListener true
                mDoneListener?.onSearchDone(queryText)
            }
            return@setOnEditorActionListener true
        }

        binding.clear.setOnClickListener {
            binding.input.setText("")
            mClearListener?.onClearText()
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        // 一般MainActivity不会被销毁，不更新会保留旧数据
        delayTime = AppConfig.searchDelay.toLong()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 预防内存泄漏
        mHandler.removeCallbacksAndMessages(null)
    }

    fun setOnAfterInputFinishListener(func: (s: String) -> Unit) {
        mFinishListener = object : OnInputFinishListener {
            override fun onInputFinish(s: String) {
                func.invoke(s)
            }
        }
    }

    fun setOnClearTextListener(func: () -> Unit) {
        mClearListener = object : OnClearTextListener {
            override fun onClearText() {
                func.invoke()
            }
        }
    }

    fun setOnSearchDoneListener(func: (s: String) -> Unit) {
        mDoneListener = object : OnSearchDoneListener {
            override fun onSearchDone(s: String) {
                func.invoke(s)
            }
        }
    }

    fun setText(s: String) {
        // 设置文本前取消监听, 否则会无限死亡回调
        binding.input.removeTextChangedListener(mTextChangeListener)
        binding.input.setText(s)
        binding.input.addTextChangedListener(mTextChangeListener)
        binding.clear.visibility = if (s.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }


    private interface OnInputFinishListener {
        fun onInputFinish(s: String)
    }

    private interface OnClearTextListener {
        fun onClearText()
    }

    private interface OnSearchDoneListener {
        fun onSearchDone(s: String)
    }


}


