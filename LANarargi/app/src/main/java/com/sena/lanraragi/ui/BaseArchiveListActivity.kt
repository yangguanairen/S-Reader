package com.sena.lanraragi.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.view.Window
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.ui.detail.DetailActivity
import com.sena.lanraragi.ui.widet.TagsViewer
import com.sena.lanraragi.utils.COVER_SHARE_ANIMATION
import com.sena.lanraragi.utils.INTENT_KEY_ARCHIVE
import com.sena.lanraragi.utils.getOrNull


/**
 * FileName: BaseArchiveListActivity
 * Author: JiaoCan
 * Date: 2024/4/19
 */

abstract class BaseArchiveListActivity(menu: Int) : BaseActivity(menu) {

    protected var mRecyclerView: RecyclerView? = null
    protected val mAdapter: MainAdapter = MainAdapter()
    // 对于所有Item共享同一份防抖
    // 即500ms内只有一个item的第一个点击会被成功响应
    private var lastItemCalledTime = -1L


    override fun onStart() {
        super.onStart()
        mRecyclerView = findViewById(R.id.recyclerView)
        initRecyclerView()
    }

    open fun onTagSelected(header: String, content: String) {

    }

    private fun initRecyclerView() {
        // 避免重复初始化RecyclerView, 导致浏览位置被重置
        if (mRecyclerView == null) {
            return
        }
        if (mRecyclerView?.layoutManager != null) {
            return
        }
        mRecyclerView?.layoutManager = getListLayoutManager()
        mRecyclerView?.adapter = mAdapter
        mAdapter.setOnItemClickListener { a, v, p ->
            val cTime = System.currentTimeMillis()
            if (cTime - lastItemCalledTime > 500) {
                lastItemCalledTime = cTime
                a.getItem(p)?.let { archive ->
                    goToDetail(v, archive)
                }
            }
        }
        mAdapter.setOnItemLongClickListener { a, _, p ->
            a.getItem(p)?.tags?.let { tags ->
                val pop = TagViewPop(this, tags)
                pop.setOnTagSelectedListener { h, c -> onTagSelected(h, c) }
                XPopup.Builder(this)
                    .isDestroyOnDismiss(true)
                    .asCustom(pop)
                    .show()
            }
            true
        }
    }

    private fun goToDetail(itemView: View, itemData: Archive) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(INTENT_KEY_ARCHIVE, itemData)
        val coverView = itemView.findViewById<View>(R.id.cover)
        val startBar = findViewById<View>(android.R.id.statusBarBackground)
        val coverPair = Pair(coverView, COVER_SHARE_ANIMATION)
        val statusPair = Pair(startBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)
        val optionBundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, coverPair, statusPair).toBundle()
        startActivity(intent, optionBundle)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mRecyclerView?.apply {
            // 保存当前的浏览状态
            val lm = getOrNull { layoutManager as LinearLayoutManager }
            val scrollPos = lm?.findFirstVisibleItemPosition() ?: 0
            val startView = getChildAt(0)
            val scrollTopOffset = if (startView == null) 0 else paddingTop - startView.top
            // 切换布局
            layoutManager = getListLayoutManager()
            // 恢复上次的浏览状态
            layoutManager?.scrollToPosition(scrollPos)
            lm?.scrollToPositionWithOffset(scrollPos, scrollTopOffset)
        }
    }

    private var lastViewMethod = AppConfig.viewMethod

    override fun onResume() {
        super.onResume()
        // 视图格式被改变
        if (lastViewMethod != AppConfig.viewMethod) {
            lastViewMethod = AppConfig.viewMethod
            mRecyclerView?.apply {
                // 保存当前的浏览状态
                val lm = getOrNull { layoutManager as LinearLayoutManager }
                val scrollPos = lm?.findFirstVisibleItemPosition() ?: 0
                val startView = getChildAt(0)
                val scrollTopOffset = if (startView == null) 0 else paddingTop - startView.top
                // 重设adapter，起到重构item的作用，否则未被回收的item布局不会改变
                adapter = mAdapter
                // 切换布局
                layoutManager = getListLayoutManager()
                // 恢复上次的浏览状态
                layoutManager?.scrollToPosition(scrollPos)
                lm?.scrollToPositionWithOffset(scrollPos, scrollTopOffset)
            }
        }

    }

    private fun getListLayoutManager(): LinearLayoutManager {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isLandCard = AppConfig.isLandCard(this)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >=
                Configuration.SCREENLAYOUT_SIZE_LARGE
        return when {
            isLandscape && isLandCard -> GridLayoutManager(this, if (isTablet) 4 else 3)
            isLandscape && !isLandCard -> GridLayoutManager(this, if (isTablet) 3 else 2)
            !isLandscape && isLandCard -> GridLayoutManager(this, if (isTablet) 3 else 2)
            !isLandscape && !isLandCard -> if (isTablet) GridLayoutManager(this, 2) else LinearLayoutManager(this)
            else -> LinearLayoutManager(this)
        }
    }

    @SuppressLint("ViewConstructor")
    class TagViewPop(context: Context, tagStr: String): CenterPopupView(context) {

        private var mListener: TagsViewer.OnTagSelectedListener? = null

        private val mTagStr = tagStr

        private lateinit var tagViewer: TagsViewer

        override fun getImplLayoutId(): Int {
            return R.layout.view_main_tag_view_popup
        }

        override fun onCreate() {
            super.onCreate()

            tagViewer = findViewById(R.id.tageViewer)
            tagViewer.setOnTagSelectedListener { header, content ->
//                val query = if (header.isBlank()) content else "$header:$content"
                dismiss()
                mListener?.onTagSelected(header, content)
            }
            tagViewer.setTags(mTagStr)
        }

        fun setOnTagSelectedListener(func: (header: String, content: String) -> Unit) {
            mListener = object : TagsViewer.OnTagSelectedListener {
                override fun onTagSelected(header: String, content: String) {
                    func.invoke(header, content)
                }
            }
        }
    }

}

