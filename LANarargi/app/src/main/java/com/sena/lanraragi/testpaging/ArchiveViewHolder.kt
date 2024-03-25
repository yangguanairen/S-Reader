package com.sena.lanraragi.testpaging

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.ItemArchiveBinding


/**
 * FileName: ArchiveViewHolder
 * Author: JiaoCan
 * Date: 2024/3/25
 */

class ArchiveViewHolder(
    private val binding: ItemArchiveBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(context: Context, archive: Archive) {
        binding.apply {
            binding.title.text = archive.title
            Glide.with(context).load("").into(binding.cover)
        }
    }
}

