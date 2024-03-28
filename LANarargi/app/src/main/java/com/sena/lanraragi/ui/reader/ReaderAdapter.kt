package com.sena.lanraragi.ui.reader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.chad.library.adapter4.BaseQuickAdapter
import com.sena.lanraragi.databinding.ItemReaderBinding
import com.sena.lanraragi.utils.ImageUtils


/**
 * FileName: ReaderPageAdapter
 * Author: JiaoCan
 * Date: 2024/3/28
 */

class ReaderAdapter : BaseQuickAdapter<String, ReaderAdapter.VH>() {


    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        item?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(ItemReaderBinding.inflate(LayoutInflater.from(context), parent, false))
    }


    inner class VH(private val binding: ItemReaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String) {
            val id = Regex("api/archives/[a-z0-9]+/page").find(url)?.value?.split(Regex("/"))?.getOrNull(2)
            val path = Regex("path=.*").find(url)?.value?.replace("path=", "")
            if (id != null && path != null) {
                ImageUtils.loadPath(context, id, path, binding.imageView)
            }
        }
    }




}

