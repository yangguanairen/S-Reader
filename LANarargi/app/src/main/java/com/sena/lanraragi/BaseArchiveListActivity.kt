package com.sena.lanraragi

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
import com.lxj.xpopup.interfaces.OnSelectListener
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.ui.MainAdapter
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


    override fun onStart() {
        super.onStart()
        mRecyclerView = findViewById(R.id.recyclerView)
        initRecyclerView()
    }

    open fun onTagSelected(s: String) {

    }

    private fun initRecyclerView() {
        // 避免重复初始化RecyclerView, 导致浏览位置被重置
        if (mRecyclerView == null) {
            return
        }
        if (mRecyclerView?.layoutManager != null) {
            return
        }

        mRecyclerView?.layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
        mRecyclerView?.adapter = mAdapter
        mAdapter.setOnItemClickListener { a, v, p ->
            a.getItem(p)?.let { archive ->
                goToDetail(v, archive)
            }
        }
        mAdapter.setOnItemLongClickListener { a, _, p ->
            a.getItem(p)?.tags?.let { tags ->
                val pop = TagViewPop(this, tags)
                pop.setOnItemClickListener { q -> onTagSelected(q) }
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
            layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                GridLayoutManager(this@BaseArchiveListActivity, 2)
            } else {
                LinearLayoutManager(this@BaseArchiveListActivity)
            }
            // 恢复上次的浏览状态
            layoutManager?.scrollToPosition(scrollPos)
            lm?.scrollToPositionWithOffset(scrollPos, scrollTopOffset)
        }
    }

    private class TagViewPop(context: Context, tagStr: String): CenterPopupView(context) {

        private var onItemClickListener: OnSelectListener? = null

        private val mTagStr = tagStr

        private lateinit var tagViewer: TagsViewer

        override fun getImplLayoutId(): Int {
            return R.layout.view_main_tag_view_popup
        }

        override fun onCreate() {
            super.onCreate()

            tagViewer = findViewById(R.id.tageViewer)
            tagViewer.setOnItemClickListener { header, content ->
                val query = if (header.isBlank()) content else "$header:$content"
                dismiss()
                onItemClickListener?.onSelect(-1, query)
            }
            tagViewer.setTags(mTagStr)
        }

        fun setOnItemClickListener(func: (s: String) -> Unit) {
            onItemClickListener = OnSelectListener { _, text ->
                text?.let { func.invoke(it) }
            }
        }
    }

}

