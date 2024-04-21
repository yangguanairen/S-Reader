package com.sena.lanraragi.ui.reader

import android.content.Context
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lxj.xpopup.core.BottomPopupView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.R
import com.sena.lanraragi.utils.DataStoreHelper
import com.sena.lanraragi.utils.ScaleType
import com.sena.lanraragi.utils.getThemeColor


/**
 * FileName: ReaderBottomPopup
 * Author: JiaoCan
 * Date: 2024/3/29
 */

class ReaderBottomPopup(context: Context) : BottomPopupView(context) {

    private val mContext = context

    private var mScaleTypeChangeListener: ScaleTypeChange? = null
    private var mOnPageSelectedListener: OnPageSelectedListener? = null
//    private var mOnGoToDetailClickListener: OnClickListener? = null
//    private var mOnSelectPageClickListener: OnClickListener? = null
//    private var mOnShowBookmarkClickListener: OnClickListener? = null

    private var fitWidthButton: TextView? = null
    private var fitHeightButton: TextView? = null
    private var fitPageButton: TextView? = null
    private var fitFlexButton: TextView? = null
    private var gotoDetailLayout: RelativeLayout? = null
    private var selectPageLayout: RelativeLayout? = null
    private var showBookmarkLayout: RelativeLayout? = null


    private val mMap by lazy {
        mapOf(
            fitWidthButton to ScaleType.FIT_WIDTH,
            fitHeightButton to ScaleType.FIT_HEIGHT,
            fitPageButton to ScaleType.FIT_PAGE,
            fitFlexButton to ScaleType.WEBTOON
        )
    }

    override fun getImplLayoutId(): Int {
        return R.layout.view_reader_bottom
    }


    override fun onCreate() {
        super.onCreate()

        val textArray = context.resources.getStringArray(R.array.setting_read_scale_select)
        fitPageButton = findViewById<TextView?>(R.id.fitPage)?.apply {
            text = textArray[0]
            setOnClickListener { changeFitStatus(ScaleType.FIT_PAGE) }
        }
        fitWidthButton = findViewById<TextView?>(R.id.fitWidth)?.apply {
            text = textArray[1]
            setOnClickListener { changeFitStatus(ScaleType.FIT_WIDTH) }
        }
        fitHeightButton = findViewById<TextView?>(R.id.fitHeight)?.apply {
            text = textArray[2]
            setOnClickListener { changeFitStatus(ScaleType.FIT_HEIGHT) }
        }
        fitFlexButton = findViewById<TextView?>(R.id.fitFlex)?.apply {
            text = textArray[3]
            setOnClickListener { changeFitStatus(ScaleType.WEBTOON) }
        }

        gotoDetailLayout = findViewById<RelativeLayout?>(R.id.goToDetail).apply {
            setOnClickListener { (activity as AppCompatActivity).onBackPressedDispatcher.onBackPressed() }
        }
        selectPageLayout = findViewById<RelativeLayout?>(R.id.selectPage).apply {
            setOnClickListener {  }
        }
        showBookmarkLayout = findViewById<RelativeLayout?>(R.id.showBookmark).apply {
            setOnClickListener {  }
        }

        initScaleType()
    }

    private fun initScaleType() {
        changeFitStatus(AppConfig.scaleMethod, false)
    }

    private fun changeFitStatus(scaleType: ScaleType, needCallback: Boolean = true) {

        mMap.forEach {
            val view = it.key
            val type = it.value
            view?.apply {
                if (scaleType == type) {
                    mContext.getThemeColor(R.attr.textColor3)?.let { color -> setTextColor(color) }
                    setBackgroundResource(R.drawable.bg_reader_fit_method_selected)
                } else {
                    mContext.getThemeColor(R.attr.textColor2)?.let { color -> setTextColor(color) }
                    setBackgroundResource(R.drawable.bg_reader_fit_method_unselect)
                }
            }

        }

        AppConfig.scaleMethod = scaleType
        DataStoreHelper.updateValue(context, DataStoreHelper.KEY.READ_SCALE_METHOD, scaleType)

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

    fun setOnPageSelectedListener(func: (page: Int) -> Unit) {
        mOnPageSelectedListener = object : OnPageSelectedListener {
            override fun onPageSelected(page: Int) {
                func.invoke(page)
            }
        }
    }

    private interface OnPageSelectedListener {
        fun onPageSelected(page: Int)
    }

    private interface ScaleTypeChange {
        fun onScaleTypeChangeListener(scaleType: ScaleType)
    }
}

