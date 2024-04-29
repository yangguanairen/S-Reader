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
import androidx.appcompat.widget.ListPopupWindow
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.databinding.ViewSearchBinding
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.getOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * FileName: SearchView
 * Author: JiaoCan
 * Date: 2024/3/26
 */

private const val WHAT_INPUT = 1
private const val WHAT_POPUP = 2

@SuppressLint("ViewConstructor", "InflateParams")
class SearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val mContext = context

    private val binding = ViewSearchBinding.inflate(LayoutInflater.from(mContext), this, true)

    private var mFinishListener: OnInputFinishListener? = null
    private var mClearListener: OnClearTextListener? = null
    private var mDoneListener: OnSearchDoneListener? = null
    private var mRelatedListener: OnRelatedSelectedListener? = null

    private var delayTime = AppConfig.searchDelay.toLong()
    private var lastJob: Job? = null


    val listPop by lazy {
        ListPopupWindow(mContext).apply {
            anchorView = this@SearchView
        }
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.what) {
                WHAT_INPUT -> {
                    val text = getOrNull { msg.obj.toString() }
                    DebugLog.i("SearchView 接收: $text")
                    if (text == null)  return
                    mFinishListener?.onInputFinish(text)
                }
                WHAT_POPUP -> {
                    lastJob?.cancel()
                    val text = getOrNull { msg.obj.toString() }
                    text?.let {
                        if (it.length >= 2) lastJob = handleRelated(it)
                        else listPop.dismiss()
                    }
                }
            }
        }
    }

    private val mTextChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.clear.visibility = if (s?.length != 0) View.VISIBLE else View.INVISIBLE
            DebugLog.i("SearchView 输入: $s")
            if (s == null) return

            val inputMessage = Message().apply {
                what = WHAT_INPUT
                obj = s
            }
            mHandler.removeMessages(WHAT_INPUT)
            mHandler.sendMessageDelayed(inputMessage, delayTime)

            // 处理关联词
            val popupMessage = Message().apply {
                what = WHAT_POPUP
                obj = s
            }
            mHandler.removeMessages(WHAT_POPUP)
            mHandler.sendMessageDelayed(popupMessage, 200)
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

    private fun handleRelated(inputText: String): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                LanraragiDB.getRelatedTags(inputText)
            }
            if (result.isEmpty()) {
                listPop.dismiss()
            } else {
                listPop.setAdapter(ListPopAdapter(mContext, result, inputText))
                listPop.setOnItemClickListener { _, _, position, _ ->
                    mHandler.removeMessages(WHAT_INPUT)
                    val s = result[position].splicingText
                    mRelatedListener?.onRelatedSelected(s)
                    listPop.dismiss()
                }
                listPop.show()
            }
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

    fun setOnRelatedSelectedListener(func: (s: String) -> Unit) {
        mRelatedListener = object : OnRelatedSelectedListener {
            override fun onRelatedSelected(s: String) {
                func.invoke(s)
            }
        }
    }

    fun setText(s: String) {
        // 设置文本前取消监听, 否则会无限死亡回调
        binding.input.removeTextChangedListener(mTextChangeListener)
        binding.input.setText(s)
        // setText后光标会自动移动至0位
        binding.input.setSelection(s.length)
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

    interface OnRelatedSelectedListener {
        fun onRelatedSelected(s: String)
    }


}


