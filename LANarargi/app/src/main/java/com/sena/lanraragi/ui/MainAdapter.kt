package com.sena.lanraragi.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseDifferAdapter
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.R
import com.sena.lanraragi.database.archiveData.Archive
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
        return if (context.getString(R.string.setting_common_view_method_select_2) == AppConfig.viewMethod) {
            VH(LayoutInflater.from(context).inflate(R.layout.item_archive_vertical, parent, false))
        } else {
            VH(LayoutInflater.from(context).inflate(R.layout.item_archive, parent, false))
        }
    }

    override fun getItemViewType(position: Int, list: List<Archive>): Int {
        return position
    }

    class VH(rootView: View) : RecyclerView.ViewHolder(rootView) {

        private val titleView = rootView.findViewById<TextView>(R.id.title)
        private val coverView = rootView.findViewById<ImageView>(R.id.cover)

        fun bind(context: Context, archive: Archive) {
                titleView.text = archive.title
                ViewCompat.setTransitionName(coverView, COVER_SHARE_ANIMATION)
                var builder = ImageLoad.Builder(context)
                    .loadThumb(archive.arcid)

                if (AppConfig.isLandCard(context)) {
                    builder = builder.setRadius(16f)
                }
                builder.into(coverView).execute()
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

