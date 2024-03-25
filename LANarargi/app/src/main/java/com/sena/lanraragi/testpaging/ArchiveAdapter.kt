package com.sena.lanraragi.testpaging

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.ItemArchiveBinding


/**
 * FileName: ArchiveAdapter
 * Author: JiaoCan
 * Date: 2024/3/25
 */

class ArchiveAdapter(private val context: Context) : PagingDataAdapter<Archive, ArchiveViewHolder>(ARTICLE_DIFF_CALLBACK) {

    companion object {
        private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Archive>() {
            override fun areItemsTheSame(oldItem: Archive, newItem: Archive): Boolean =
                oldItem.arcid == newItem.arcid

            override fun areContentsTheSame(oldItem: Archive, newItem: Archive): Boolean =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(context, item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        return ArchiveViewHolder(
            ItemArchiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
}

