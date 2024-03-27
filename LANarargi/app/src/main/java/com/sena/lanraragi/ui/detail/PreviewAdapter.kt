package com.sena.lanraragi.ui.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.sena.lanraragi.databinding.ItemPreviewBinding
import com.sena.lanraragi.utils.ImageUtils


/**
 * FileName: PreviewAdapter
 * Author: JiaoCan
 * Date: 2024/3/27
 */

class PreviewAdapter : BaseQuickAdapter<String, PreviewAdapter.VH>() {

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        if (item != null) {
            holder.bind(context, item, position)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(ItemPreviewBinding.inflate(LayoutInflater.from(context), null, false))
    }

    override fun getItemViewType(position: Int, list: List<String>): Int {
        return position
    }


    class VH(private val binding: ItemPreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(context: Context, url: String, pos: Int) {
            binding.page.text  = pos.toString()
            val id = Regex("api/archives/[a-z0-9]+/page").find(url)?.value?.split(Regex("/"))?.getOrNull(2)
            val path = Regex("path=.*").find(url)?.value?.replace("path=", "")
            if (id.isNullOrBlank() || path.isNullOrBlank()) {
                return
            }
            ImageUtils.loadPreview(context, id, path, binding.image)
        }

    }
}

