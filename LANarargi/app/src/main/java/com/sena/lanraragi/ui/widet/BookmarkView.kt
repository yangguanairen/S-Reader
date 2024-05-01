package com.sena.lanraragi.ui.widet

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.google.android.material.snackbar.Snackbar
import com.lxj.xpopup.XPopup
import com.sena.lanraragi.R
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.ui.BaseArchiveListActivity
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.database.bookmarkData.Bookmark
import com.sena.lanraragi.databinding.ItemBookmarkBinding
import com.sena.lanraragi.databinding.ViewBookmarkBinding
import com.sena.lanraragi.utils.ImageLoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * FileName: BookmarkView
 * Author: JiaoCan
 * Date: 2024/4/22
 */

@SuppressLint("ViewConstructor", "InflateParams")
class BookmarkView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val mContext = context

    private val binding = ViewBookmarkBinding.inflate(LayoutInflater.from(mContext), this, true)
    private val mAdapter = BookmarkAdapter()

    private var mOnItemClickListener: ((archive: Archive) -> Unit)? = null
    private var mOnTagSelectedListener: TagsViewer.OnTagSelectedListener? = null

    private val simpleTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.absoluteAdapterPosition
            val data = mAdapter.getItem(pos) ?: return
            val id = data.id
            mAdapter.removeAt(pos)
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    LanraragiDB.updateBookmark(id, false)
                }
                showSnackBar(mContext.getString(R.string.bookmark_view_clear_single)) {
                    mAdapter.add(pos, data)
                    CoroutineScope(Dispatchers.IO).launch {
                        LanraragiDB.updateBookmark(id, true)
                    }
                }
            }
        }
    }

    init {
        initView()
    }

    private fun initView() {
        mAdapter.setOnItemClickListener { a, _, p ->
            a.getItem(p)?.id?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    val result = withContext(Dispatchers.IO) {
                        LanraragiDB.queryArchiveById(it)
                    }
                    if (result != null) {
                        mOnItemClickListener?.invoke(result)
                    }
                }
            }
        }
        mAdapter.setOnItemLongClickListener { a, _, p ->
            a.getItem(p)?.tags?.let { tags ->
                val pop = BaseArchiveListActivity.TagViewPop(mContext, tags)
                pop.setOnTagSelectedListener { h, c ->
                    mOnTagSelectedListener?.onTagSelected(h, c)
                }
                XPopup.Builder(mContext)
                    .isDestroyOnDismiss(true)
                    .asCustom(pop)
                    .show()
            }
            true
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = mAdapter
            val itemTouchHelper = ItemTouchHelper(simpleTouchCallback)
            itemTouchHelper.attachToRecyclerView(this)
            val divider = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
            ContextCompat.getDrawable(mContext, R.drawable.bg_divider)?.let {
                divider.setDrawable(it)
            }
            addItemDecoration(divider)
        }

        binding.clearAll.setOnClickListener {
            val curData = mAdapter.items
            if (curData.isEmpty()) return@setOnClickListener
            mAdapter.submitList(emptyList())
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    curData.forEach {
                        LanraragiDB.updateBookmark(it.id, false)
                    }
                }
                showSnackBar(mContext.getString(R.string.bookmark_view_clear_all)) {
                    mAdapter.submitList(curData)
                    CoroutineScope(Dispatchers.IO).launch {
                        curData.forEach {
                            LanraragiDB.updateBookmark(it.id, true)
                        }
                    }
                }
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            initData()
        }
    }

    private fun initData() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                LanraragiDB.queryAllBookmark()
            }
            mAdapter.submitList(result)
        }
    }

    private fun showSnackBar(title: String, onRevoke: () -> Unit) {
        Snackbar.make(this@BookmarkView, title, Snackbar.LENGTH_SHORT)
            .setAction(mContext.getString(R.string.bookmark_view_revoke)) {
                onRevoke.invoke()
            }
            .show()
    }

    fun setOnItemClickListener(func: (archive: Archive) -> Unit) {
        mOnItemClickListener = func
    }

    fun setOnTagSelectedListener(func: (header: String, content: String) -> Unit) {
        mOnTagSelectedListener = object : TagsViewer.OnTagSelectedListener {
            override fun onTagSelected(header: String, content: String) {
                func.invoke(header, content)
            }
        }
    }

    class BookmarkAdapter : BaseQuickAdapter<Bookmark, BookmarkAdapter.VH>() {

        override fun onBindViewHolder(holder: VH, position: Int, item: Bookmark?) {
            item?.let { holder.onBind(context, it) }
        }

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(ItemBookmarkBinding.inflate(LayoutInflater.from(context), parent, false))
        }

        class VH(private val itemBinding: ItemBookmarkBinding) : RecyclerView.ViewHolder(itemBinding.root) {

            fun onBind(context: Context, bookmark: Bookmark) {
                itemBinding.title.text = bookmark.title
                itemBinding.readCount.text = (bookmark.progress ?: 0).toString()
                ImageLoad.Builder(context)
                    .loadThumb(bookmark.id)
                    .into(itemBinding.cover)
                    .execute()
            }
        }


    }

}