package com.sena.lanraragi.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.ItemArchiveBinding
import com.sena.lanraragi.utils.ImageUtils


/**
 * FileName: MainAdapter
 * Author: JiaoCan
 * Date: 2024/3/25 15:53
 */

class MainAdapter : BaseQuickAdapter<Archive, MainAdapter.VH>() {

    override fun onBindViewHolder(holder: VH, position: Int, item: Archive?) {
        if (item != null) {
            holder.bind(context, item)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(
            ItemArchiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemViewType(position: Int, list: List<Archive>): Int {
        return position
    }

    class VH(
        private val binding: ItemArchiveBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(context: Context, archive: Archive) {
            binding.apply {
                binding.title.text = archive.title
                ImageUtils.loadThumbTo2View(context, archive.arcid, binding.cover, binding.cover2)

            }
        }
    }
}

