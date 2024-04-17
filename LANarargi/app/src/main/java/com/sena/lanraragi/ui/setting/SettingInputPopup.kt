package com.sena.lanraragi.ui.setting

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.sena.lanraragi.R


/**
 * FileName: SettingInputPopup
 * Author: JiaoCan
 * Date: 2024/4/17
 */

@SuppressLint("ViewConstructor")
class SettingInputPopup(
    context: Context, title: String, onlyNumber: Boolean = false
) : CenterPopupView(context) {

    private val mTitle = title
    private val mOnlyNumber = onlyNumber

    private lateinit var titleView: TextView
    private lateinit var inputView: EditText
    private lateinit var cancelButton: TextView
    private lateinit var confirmButton: TextView

    private var onCancelClickListener: OnClickListener? = null
    private var onConfirmClickListener: OnInputConfirmListener? = null

    override fun getImplLayoutId(): Int {
        return R.layout.view_setting_input_popup
    }

    override fun onCreate() {
        super.onCreate()

        titleView = findViewById(R.id.tv_title)
        inputView = findViewById(R.id.et_input)
        cancelButton = findViewById(R.id.tv_cancel)
        confirmButton = findViewById(R.id.tv_confirm)

        titleView.text = mTitle
        if (mOnlyNumber) {
            inputView.inputType = InputType.TYPE_CLASS_NUMBER
        }

        cancelButton.setOnClickListener {
            dismiss()
            onCancelClickListener?.onClick(it)
        }
        confirmButton.setOnClickListener {
            dismiss()
            onConfirmClickListener?.onConfirm(inputView.text.toString())
        }
    }

//    fun setOnCancelClickListener(func: () -> Unit) {
//        onCancelClickListener = OnClickListener { func.invoke() }
//    }

    fun setOnConfirmClickListener(func: (s: String) -> Unit) {
        onConfirmClickListener = OnInputConfirmListener {
            func.invoke(it)
        }
    }

    fun setInputContent(s: String) {
        inputView.setText(s)
        inputView.setSelection(s.length)
    }

}

