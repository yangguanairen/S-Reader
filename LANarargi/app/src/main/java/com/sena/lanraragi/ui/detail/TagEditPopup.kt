package com.sena.lanraragi.ui.detail

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.lxj.xpopup.core.CenterPopupView
import com.sena.lanraragi.R
import com.sena.lanraragi.utils.getOrNull
import com.sena.lanraragi.utils.toast


/**
 * FileName: TagEditPopup
 * Author: JiaoCan
 * Date: 2024/5/5
 */

@SuppressLint("ViewConstructor")
class TagEditPopup(context: Context) : CenterPopupView(context) {

    private val mAdapter = TagEditAdapter()
    private lateinit var mRecyclerView: RecyclerView
    private var mListener: OnConfirmClickListener? = null

    override fun getImplLayoutId(): Int = R.layout.view_tag_edit_popup

    override fun onCreate() {
        super.onCreate()

        findViewById<TextView>(R.id.cancel).setOnClickListener {
            dismiss()
        }
        findViewById<TextView>(R.id.confirm).setOnClickListener {
            val list = getOrNull { mAdapter.items.subList(0, mAdapter.items.size - 1) } ?: emptyList()
            val finTags = list.joinToString(",") { "${it.first}:${it.second}" }
            mListener?.onConfirm(finTags)
            dismiss()
        }

        mRecyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mAdapter
        }
        mAdapter.addOnItemChildClickListener(R.id.operate) { a, _, p ->
            if (p == a.itemCount - 1) {
                val headerView = mRecyclerView.getChildAt(p).findViewById<EditText>(R.id.header)
                val header = headerView.text.toString()
                val contentView = mRecyclerView.getChildAt(p).findViewById<EditText>(R.id.content)
                val content = contentView.text.toString()
                if (header.isBlank() || content.isBlank()) {
                    toast(R.string.detail_tag_add_verify_failed)
                    return@addOnItemChildClickListener
                }
                headerView.setText("")
                contentView.setText("")
                a.add(p, Pair(header,content))
            } else {
                a.removeAt(p)
            }
        }
    }

    private fun parseTagsToPair(tags: String): ArrayList<Pair<String, String>> {
        val result = arrayListOf<Pair<String, String>>()
        val regex = Regex(",\\s*")

        tags.split(regex).forEach {
            val itemArr = it.split(Regex(":"), 2)
            val key = if (itemArr.size == 2) itemArr[0] else ""
            val value = if (itemArr.size == 2) itemArr[1] else it

            result.add(Pair(key, value))
        }
        return result
    }

    fun setTags(tags: String) {
        val result = parseTagsToPair(tags).apply { add(Pair("", "")) }
        mRecyclerView.adapter = mAdapter
        mAdapter.submitList(result)
    }

    fun setOnConfirmClickListener(func: (tags: String) -> Unit) {
        mListener = object : OnConfirmClickListener {
            override fun onConfirm(tags: String) {
                func.invoke(tags)
            }
        }
    }

    private interface OnConfirmClickListener {
        fun onConfirm(tags: String)
    }

    class TagEditAdapter : BaseQuickAdapter<Pair<String, String>, QuickViewHolder>() {

        override fun getItemViewType(position: Int, list: List<Pair<String, String>>): Int {
            return if (position == list.size - 1) -1 else 1
        }

        override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: Pair<String, String>?) {
            if (item == null) return
            val header = item.first
            val content = item.second
            val itemType = getItemViewType(position)
            if (itemType == -1) {
                holder.getView<ImageView>(R.id.operate).setImageResource(R.drawable.ic_add_24)
                holder.getView<EditText>(R.id.header).isEnabled = true
                holder.getView<EditText>(R.id.content).isEnabled = true
            } else {
                holder.getView<EditText>(R.id.header).setText(header)
                holder.getView<EditText>(R.id.content).setText(content)
            }
        }

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): QuickViewHolder {
            return QuickViewHolder(R.layout.item_tag_edit, parent)
        }
    }
}

