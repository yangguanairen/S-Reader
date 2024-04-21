package com.sena.lanraragi.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseDifferAdapter
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.ItemArchiveBinding
import com.sena.lanraragi.utils.COVER_SHARE_ANIMATION
import com.sena.lanraragi.utils.ImageLoad


/**
 * FileName: MainAdapter
 * Author: JiaoCan
 * Date: 2024/3/25 15:53
 */

class MainAdapter : BaseDifferAdapter<Archive, MainAdapter.VH>(DiffCallback()) {

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
                ViewCompat.setTransitionName(binding.cover, COVER_SHARE_ANIMATION)
                ImageLoad.Builder(context)
                    .loadThumb(archive.arcid)
                    .into(binding.cover)
                    .execute()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Archive>() {
        override fun areItemsTheSame(oldItem: Archive, newItem: Archive): Boolean {
            return oldItem.arcid == newItem.arcid
        }

        override fun areContentsTheSame(oldItem: Archive, newItem: Archive): Boolean {
            return oldItem.arcid == newItem.arcid
        }


    }
}

