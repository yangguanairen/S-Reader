package com.sena.lanraragi.ui

import android.annotation.SuppressLint
import android.content.Context
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.interfaces.OnSelectListener
import com.sena.lanraragi.R
import com.sena.lanraragi.ui.widet.TagsViewer


/**
 * FileName: MainTagsViewerPopup
 * Author: JiaoCan
 * Date: 2024/4/18
 */

@SuppressLint("ViewConstructor")
class MainTagsViewerPopup(context: Context, tagStr: String) : CenterPopupView(context) {

    private var onItemClickListener: OnSelectListener? = null

    private val mTagStr = tagStr

    private lateinit var tagViewer: TagsViewer

    override fun getImplLayoutId(): Int {
        return R.layout.view_main_tag_view_popup
    }


    override fun onCreate() {
        super.onCreate()

        tagViewer = findViewById(R.id.tageViewer)
        tagViewer.setOnItemClickListener { header, content ->
            val query = if (header.isBlank()) content else "$header:$content"
            dismiss()
            onItemClickListener?.onSelect(-1, query)
        }
        tagViewer.setTags(mTagStr)
    }

    fun setOnItemClickListener(func: (s: String) -> Unit) {
        onItemClickListener = OnSelectListener { _, text ->
            text?.let { func.invoke(it) }
        }
    }
}

