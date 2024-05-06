package com.sena.lanraragi.ui.detail

import android.annotation.SuppressLint
import android.app.SearchManager.OnDismissListener
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.lxj.xpopup.core.CenterPopupView
import com.sena.lanraragi.R
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.category.Category
import com.sena.lanraragi.utils.NewHttpHelper
import com.sena.lanraragi.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * FileName: CategoryEditPopup
 * Author: JiaoCan
 * Date: 2024/5/5
 */

@SuppressLint("ViewConstructor")
class CategoryEditPopup(context: Context, arcId: String) : CenterPopupView(context) {

    private val mArcId = arcId
    private val mAdapter = CategoryEditAdapter()
    private lateinit var mRecyclerView: RecyclerView
    private var mListener: OnDismissListener? = null

    private val allJob: ArrayList<Job> = arrayListOf()

    private val simpleTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.absoluteAdapterPosition
            val id = mAdapter.getItem(pos)?.id ?: return
            CoroutineScope(Dispatchers.Main).launch {
                val isSuccess = withContext(Dispatchers.IO) {
                    NewHttpHelper.deleteCategory(id)
                }
                if (!isSuccess) {
                    toast(R.string.detail_category_delete_failed)
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    LanraragiDB.deleteCategory(id)
                }
                mAdapter.removeAt(pos)
            }
        }
    }

    override fun getImplLayoutId(): Int = R.layout.view_category_edit_popup

    override fun onCreate() {
        super.onCreate()

        findViewById<TextView>(R.id.finish).apply {
            setOnClickListener { dismiss() }
        }

        mRecyclerView = findViewById<RecyclerView?>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            val itemTouchHelper = ItemTouchHelper(simpleTouchCallback)
            itemTouchHelper.attachToRecyclerView(this)
            adapter = mAdapter
        }
        mAdapter.addOnItemChildClickListener(R.id.checkBox) { a, v, p ->
            val checkBox = v as CheckBox
            val id = a.getItem(p)?.id ?: return@addOnItemChildClickListener
            handleCheckBox(checkBox, id)
        }
        mAdapter.addOnItemChildClickListener(R.id.create) { a, v, p ->
            val contentView = mRecyclerView.getChildAt(p).findViewById<EditText>(R.id.content)
            val content = contentView.text.toString()
            val job = CoroutineScope(Dispatchers.Main).launch {
                val categoryId = withContext(Dispatchers.IO) {
                    NewHttpHelper.createCategory(content)
                }
                if (categoryId.isNullOrBlank()) {
                    toast(R.string.detail_category_create_failed)
                    return@launch
                }
                val newCategory = Category(categoryId, content, emptyList(), 0-1, 0, "")
                withContext(Dispatchers.IO) {
                    LanraragiDB.addNewCategory(newCategory)
                }
                contentView.setText("")
                mAdapter.add(p, newCategory)
            }
            job.invokeOnCompletion { allJob.remove(job) }
            allJob.add(job)
        }
    }

    override fun onDismiss() {
        super.onDismiss()
        allJob.forEach { it.cancel() }
        mListener?.onDismiss()
    }

    private fun handleCheckBox(checkBox: CheckBox, categoryId: String) {
        val curStatus = checkBox.isChecked
        val job = CoroutineScope(Dispatchers.Main).launch {
            val isSuccess = withContext(Dispatchers.IO) {
                if (curStatus) {
                    NewHttpHelper.addArchiveToCategory(mArcId, categoryId)
                } else {
                    NewHttpHelper.removeArchiveFromCategory(mArcId, categoryId)
                }
            }
            if (isSuccess) {
                withContext(Dispatchers.IO) {
                    if (curStatus) {
                        LanraragiDB.addArchiveToCategory(mArcId, categoryId)
                    } else {
                        LanraragiDB.removeArchiveFromCategory(mArcId, categoryId)
                    }
                }
            } else {
                toast(R.string.detail_category_update_failed)
            }
        }
        job.invokeOnCompletion { allJob.remove(job) }
        allJob.add(job)
    }

    fun setCategories(list: List<Category>) {
        mRecyclerView.adapter = mAdapter
        val finList = list.toMutableList()
        finList.add(Category("", "", emptyList(), -1, 0, ""))
        mAdapter.submitList(finList)
    }

    fun setOnDismissListener(func: () -> Unit) {
        mListener = OnDismissListener { func.invoke() }
    }


    inner class CategoryEditAdapter : BaseQuickAdapter<Category, QuickViewHolder>() {

        override fun getItemViewType(position: Int, list: List<Category>): Int {
            return if (position == list.size - 1) -1 else 1
        }

        override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: Category?) {
            if (item == null) return
            if (getItemViewType(position) == -1) {
                holder.getView<EditText>(R.id.content).apply {
                    isEnabled = true
                }
                holder.getView<ImageView>(R.id.create).visibility = View.VISIBLE
                holder.getView<CheckBox>(R.id.checkBox).visibility = View.GONE
            } else {
                holder.getView<EditText>(R.id.content).setText(item.name)
                holder.getView<EditText>(R.id.header).setText(
                    context.getString(if (item.pinned == 0) R.string.detail_category_pinned_0 else R.string.detail_category_pinned_1)
                )
                val isCheck = item.archives.contains(mArcId)
                holder.getView<CheckBox>(R.id.checkBox).isChecked = isCheck
            }
        }

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): QuickViewHolder {
            return QuickViewHolder(R.layout.item_category_edit, parent)
        }

    }

}

