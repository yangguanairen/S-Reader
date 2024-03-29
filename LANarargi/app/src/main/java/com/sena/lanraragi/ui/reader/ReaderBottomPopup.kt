package com.sena.lanraragi.ui.reader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.core.PositionPopupView
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ViewReaderBottomBinding
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.getThemeColor


/**
 * FileName: ReaderBottomPopup
 * Author: JiaoCan
 * Date: 2024/3/29
 */

class ReaderBottomPopup(context: Context) : BottomPopupView(context) {

    private val mContext = context

    private var mScaleTypeChangeListener: ScaleTypeChange? = null

    private var fitWidthButton: TextView? = null
    private var fitHeightButton: TextView? = null
    private var fitPageButton: TextView? = null
    private var fitFlexButton: TextView? = null

    override fun getImplLayoutId(): Int {
        return R.layout.view_reader_bottom
    }


    override fun onCreate() {
        super.onCreate()

        fitWidthButton = findViewById<TextView?>(R.id.fitWidth)?.apply { setOnClickListener { changeFitStatus(ScaleType.FIT_WIDTH) } }
        fitHeightButton = findViewById<TextView?>(R.id.fitHeight)?.apply { setOnClickListener { changeFitStatus(ScaleType.FIT_HEIGHT) } }
        fitPageButton = findViewById<TextView?>(R.id.fitPage)?.apply { setOnClickListener { changeFitStatus(ScaleType.FIT_PAGE) } }
        fitFlexButton = findViewById<TextView?>(R.id.fitFlex)?.apply { setOnClickListener { changeFitStatus(ScaleType.WEBTOON) } }


        initScaleType()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initScaleType() {
        // TODO: 从设置中读取当前scaleType
        changeFitStatus(ScaleType.FIT_WIDTH, false)
    }

    private fun changeFitStatus(scaleType: ScaleType, needCallback: Boolean = true) {

        val values = ScaleType.values()
        val list = arrayListOf(
            fitWidthButton, fitHeightButton, fitPageButton, fitFlexButton
        )
        values.forEachIndexed { index, type ->
            val view = list[index]
            view?.apply {
                if (scaleType == type) {
                    mContext.getThemeColor(R.attr.textColor3)?.let { color -> view.setTextColor(color) }
                    view.setBackgroundResource(R.drawable.bg_reader_fit_method_selected)
                } else {
                    mContext.getThemeColor(R.attr.textColor2)?.let { color -> view.setTextColor(color) }
                    view.setBackgroundResource(R.drawable.bg_reader_fit_method_unselect)
                }
            }
        }

        // TODO: scaleType写回到设置中
        if (needCallback) {
            mScaleTypeChangeListener?.onScaleTypeChangeListener(scaleType)
        }
    }

    fun setOnScaleTypeChangeListener(func: (scaleType: ScaleType) -> Unit) {
        mScaleTypeChangeListener = object : ScaleTypeChange {
            override fun onScaleTypeChangeListener(scaleType: ScaleType) {
                func.invoke(scaleType)
            }
        }
    }


    private interface ScaleTypeChange {
        fun onScaleTypeChangeListener(scaleType: ScaleType)
    }

    enum class ScaleType {
        FIT_WIDTH,
        FIT_HEIGHT,
        FIT_PAGE,
        WEBTOON
    }


}

