package com.sena.lanraragi.ui.reader

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseDifferAdapter
import com.sena.lanraragi.utils.ScaleType


/**
 * FileName: NewReaderAdapter
 * Author: JiaoCan
 * Date: 2024/4/24
 */

class NewReaderAdapter : BaseDifferAdapter<String, NewReaderAdapter.VH>(DiffCallback()) {

    private val loadPages = arrayListOf<ReaderPageFragment>()
    private  var mOnClickListener: View.OnClickListener? = null
    private var mOnLongClickListener: View.OnLongClickListener? = null

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        item?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        val fragment = ReaderPageFragment()
        return VH(fragment)
    }

    fun setOnImageClickListener(func: () -> Unit)  {
        mOnClickListener = View.OnClickListener { func.invoke() }
    }

    fun setOnImageLongClickListener(func: () -> Boolean)  {
        mOnLongClickListener = View.OnLongClickListener { func.invoke() }
    }

    fun onScaleTypeChange(scaleType: ScaleType) {
        loadPages.forEach { it.onScaleTypeChange(scaleType) }
    }

    inner class VH(private val fragment: ReaderPageFragment): RecyclerView.ViewHolder(fragment.testGetView()) {

        fun bind(url: String) {
            fragment.apply {
                setOnAttachListener { loadPages.add(fragment) }
                setOnDetachListener { loadPages.remove(fragment) }
                setOnTapListener { mOnClickListener?.onClick(null) }
                setOnLongPressListener { mOnLongClickListener?.onLongClick(null) == true }
                setUrl(url)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }


}

