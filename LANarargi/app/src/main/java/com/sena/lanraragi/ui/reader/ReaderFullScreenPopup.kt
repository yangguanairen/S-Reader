package com.sena.lanraragi.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.widget.NumberPicker
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.impl.FullScreenPopupView
import com.sena.lanraragi.R
import com.sena.lanraragi.ui.detail.PreviewAdapter


/**
 * FileName: ReaderFullScreenPopup
 * Author: JiaoCan
 * Date: 2024/4/23
 */

@SuppressLint("ViewConstructor")
class ReaderFullScreenPopup(context: Context, pos: Int, list: List<Pair<String, String>>) : FullScreenPopupView(context) {

    private val mPos = pos
    private val mList = list

    private var mOnPageSelectedListener: OnPageSelectedListener? = null


    private val mAdapter: PreviewAdapter = PreviewAdapter()
    private lateinit var pagePicker: NumberPicker
    private lateinit var mRecyclerView: RecyclerView

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        private var isHuman = false

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lm = recyclerView.layoutManager as LinearLayoutManager
            val firstVisiblePos = lm.findFirstVisibleItemPosition()
            val lastVisiblePos = lm.findLastVisibleItemPosition()
            val finalPos = if (dy >= 0) lastVisiblePos else firstVisiblePos
            if (isHuman) {
                pagePicker.value = finalPos + 1
                isHuman = false
            }
        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) { // 判断是否是用户主动进行的拖动
                isHuman = true
            }
        }
    }

    override fun getInnerLayoutId(): Int {
        return R.layout.view_reader_full_screnn_pop
    }

    override fun onCreate() {
        super.onCreate()

        pagePicker = findViewById(R.id.pagePicker)
        mRecyclerView = findViewById(R.id.recyclerView)

        pagePicker.apply {
            minValue = 1
            maxValue = mList.size
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnScrollListener { view, scrollState ->
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    mRecyclerView.scrollToPosition(view.value - 1)
                }
            }
        }

        mAdapter.setOnItemClickListener { _, _, p ->
            dismiss()
            mOnPageSelectedListener?.onPageSelected(p)
        }
        mRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            addOnScrollListener(onScrollListener)
            adapter = mAdapter
        }

        // TODO: 暂且注销，迁移previewThumb中
        mAdapter.submitList(mList)
        pagePicker.value = mPos + 1
        mRecyclerView.scrollToPosition(mPos)
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
}

