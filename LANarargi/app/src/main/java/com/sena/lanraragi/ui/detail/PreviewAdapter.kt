package com.sena.lanraragi.ui.detail

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.sena.lanraragi.databinding.ItemPreviewBinding
import com.sena.lanraragi.utils.ImageLoad


/**
 * FileName: PreviewAdapter
 * Author: JiaoCan
 * Date: 2024/3/27
 */

class PreviewAdapter : BaseQuickAdapter<Pair<String, String>, PreviewAdapter.VH>() {

    override fun onBindViewHolder(holder: VH, position: Int, item: Pair<String, String>?) {
        item?.let {
            val arcId = item.first
            val pageName = item.second
            holder.bind(context, arcId, pageName, position)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(ItemPreviewBinding.inflate(LayoutInflater.from(context), null, false))
    }

    override fun getItemViewType(position: Int, list: List<Pair<String, String>>): Int {
        return position
    }


    class VH(private val binding: ItemPreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(context: Context, id: String, pageName: String, pos: Int) {
            binding.page.text  = (pos + 1).toString()
            ImageLoad.Builder(context)
                .loadPreview(id, pageName)
                // 服务端可能解压失败，返回默认的无缩略图图片
                .isIgnoreDiskCache(true)
                .into(binding.image)
                .execute()
        }

    }
}

