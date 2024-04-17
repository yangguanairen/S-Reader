package com.sena.lanraragi.ui.setting

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.get
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.interfaces.OnSelectListener
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ItemSettingSelectPopupBinding


/**
 * FileName: SettingSelectPopup
 * Author: JiaoCan
 * Date: 2024/4/17
 */

@SuppressLint("ViewConstructor")
class SettingSelectPopup(context: Context, title: String, list: List<String>) : CenterPopupView(context) {

    private val mTitle = title
    private val mList = list

    private var mOnCancelClickListener: OnClickListener? = null
    private var mOnSelectedListener: OnSelectListener? = null

    private lateinit var titleView: TextView
    private lateinit var listLayout: LinearLayout
    private lateinit var cancelButton: TextView

    override fun getImplLayoutId(): Int {
        return R.layout.view_setting_select_popup
    }

    override fun onCreate() {
        super.onCreate()

        titleView = findViewById(R.id.tv_title)
        listLayout = findViewById(R.id.radioList)
        cancelButton = findViewById(R.id.tv_cancel)

        titleView.text = mTitle
        mList.forEachIndexed { index, s ->
            val itemBinding = ItemSettingSelectPopupBinding.inflate(LayoutInflater.from(context), listLayout, true)
            itemBinding.radioButton.apply {
                text = s
//                compoundDrawablePadding = context.dp2px(16)
//                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, context.dp2px(32), 0)
            }
            itemBinding.radioLayout.setOnClickListener {
                dismiss()
                mOnSelectedListener?.onSelect(index, s)
            }
        }
        cancelButton.setOnClickListener {
            dismiss()
            mOnCancelClickListener?.onClick(it)
        }
    }

    fun setOnSelectedListener(func: (p: Int, s: String) -> Unit) {
        mOnSelectedListener = OnSelectListener { position, text ->
            func.invoke(position, text)
        }
    }

//    fun setOnCancelClickListener(func: () -> Unit) {
//        mOnCancelClickListener = OnClickListener {
//            func.invoke()
//        }
//    }

    fun updateSelected(s: String) {
        val index = mList.indexOf(s)
        val radioCount = listLayout.childCount
        if (index < 0 || index >= radioCount) return

        for (i in 0 until radioCount) {
            val radioLayout = listLayout[i] as LinearLayout
            val radio = radioLayout.findViewById<RadioButton>(R.id.radioButton)
            radio.isChecked = (i == index)
        }
    }
}

