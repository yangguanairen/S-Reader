package com.sena.lanraragi.ui.setting

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.core.view.get
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.interfaces.OnSelectListener
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ItemSettingSelectPopupBinding
import com.sena.lanraragi.utils.getThemeColor


/**
 * FileName: SettingSelectPopup
 * Author: JiaoCan
 * Date: 2024/4/17
 */

@SuppressLint("ViewConstructor")
class SettingSelectPopup(context: Context, @StringRes titleId: Int, @ArrayRes strArrayId: Int) : CenterPopupView(context) {

    private val mTitleId = titleId
    private val mStrArrayId = strArrayId
    private var mList: Array<String> = emptyArray()

    private var mOnCancelClickListener: OnClickListener? = null
    private var mOnSelectedListener: OnSelectListener? = null

    private lateinit var rootLayout: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var listLayout: LinearLayout
    private lateinit var cancelButton: TextView

    override fun getImplLayoutId(): Int {
        return R.layout.view_setting_select_popup
    }

    override fun onCreate() {
        super.onCreate()

        rootLayout = findViewById(R.id.rootLayout)
        titleView = findViewById(R.id.tv_title)
        listLayout = findViewById(R.id.radioList)
        cancelButton = findViewById(R.id.tv_cancel)

        titleView.text = context.getString(mTitleId)
        mList = resources.getStringArray(mStrArrayId)
        mList.forEachIndexed { index, s ->
            val itemBinding = ItemSettingSelectPopupBinding.inflate(LayoutInflater.from(context), listLayout, true)
            itemBinding.radioButton.text = s
            itemBinding.radioLayout.setOnClickListener {
                dismiss()
                mOnSelectedListener?.onSelect(index, mList[index])
            }
        }
        cancelButton.setOnClickListener {
            dismiss()
            mOnCancelClickListener?.onClick(it)
        }
    }

    // 应对多主题
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        context.getThemeColor(R.attr.textColor1)?.let {
            titleView.setTextColor(it)
            for (i in 0 until listLayout.childCount) {
                val itemView = (listLayout.getChildAt(i) as LinearLayout).getChildAt(0) as RadioButton
                itemView.setTextColor(it)
            }
        }
        context.getThemeColor(R.attr.textColor3)?.let {
            cancelButton.setTextColor(it)
        }
        context.theme.getDrawable(R.drawable.bg_popup)?.let {
            rootLayout.background = it
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
        mList = resources.getStringArray(mStrArrayId)
        val index = mList.indexOf(s)
        val radioCount = listLayout.childCount
        if (index < 0 || index >= radioCount) return

        for (i in 0 until radioCount) {
            val radioLayout = listLayout[i] as LinearLayout
            val radio = radioLayout.findViewById<RadioButton>(R.id.radioButton)
            radio.text = mList[i]
            radio.isChecked = (i == index)
        }
    }
}

