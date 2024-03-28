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

    private var mFinishListener: AfterInputFinishListener? = null
    private var mClearListener: ClearTextListener? = null
    private var mDoneListener: SearchDoneListener? = null

    private val mWhat = 1
    private val delayTime = 500L
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val what = msg.what
            if (what != mWhat) return
            val text = getOrNull { msg.obj.toString() }
            DebugLog.d("SearchView 接收: $text")
            if (text == null)  return
            mFinishListener?.onAfterInputFinishListener(text)
        }
    }

    init {
        initView()
    }


    private fun initView() {

        binding.input.setOnClickListener {

        }

        binding.input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                DebugLog.d("SearchView 机器人: false")
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
        })

        binding.input.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val queryText = v.editableText?.toString() ?: return@setOnEditorActionListener true
                mDoneListener?.onSearchDoneListener(queryText)
            }
            return@setOnEditorActionListener true
        }

        binding.clear.setOnClickListener {
            binding.input.setText("")
            mClearListener?.onClearTextListener()
        }
    }

    fun setOnAfterInputFinishListener(func: (s: String) -> Unit) {
        mFinishListener = object : AfterInputFinishListener {
            override fun onAfterInputFinishListener(s: String) {
                func.invoke(s)
            }
        }
    }

    fun setOnClearTextListener(func: () -> Unit) {
        mClearListener = object : ClearTextListener {
            override fun onClearTextListener() {
                func.invoke()
            }
        }
    }

    fun setOnSearchDoneListener(func: (s: String) -> Unit) {
        mDoneListener = object : SearchDoneListener {
            override fun onSearchDoneListener(s: String) {
                func.invoke(s)
            }
        }
    }

    fun setText(s: String) {
        binding.input.setText(s)
        binding.clear.visibility = if (s.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }


    private interface AfterInputFinishListener {
        fun onAfterInputFinishListener(s: String)
    }

    private interface ClearTextListener {
        fun onClearTextListener()
    }

    private interface SearchDoneListener {
        fun onSearchDoneListener(s: String)
    }


}


