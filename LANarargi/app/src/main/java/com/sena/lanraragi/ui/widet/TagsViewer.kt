package com.sena.lanraragi.ui.widet

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.flexbox.FlexboxLayout
import com.sena.lanraragi.R
import com.sena.lanraragi.database.category.Category
import com.sena.lanraragi.databinding.ItemTagBinding
import com.sena.lanraragi.databinding.ItemTagLayoutBinding
import com.sena.lanraragi.databinding.ViewTagsBinding
import com.sena.lanraragi.utils.getOrNull
import java.util.Calendar


/**
 * FileName: ViewTagsViewer
 * Author: JiaoCan
 * Date: 2024/3/26
 */

@SuppressLint("ViewConstructor", "InflateParams")
class TagsViewer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val mContext = context

    private val binding = ViewTagsBinding.inflate(LayoutInflater.from(mContext), this, true)

    private var mTags: String? = null
    private var mListener: OnTagSelectedListener? = null

    fun setTags(tags: String?) {
        mTags = tags
        if (tags.isNullOrBlank()) return
        redrawTagView(tags)
    }

    fun setCategories(category: List<Category>) {
        binding.tagLayout.removeAllViews()
        category.forEach {
            val header = context.getString(if (it.pinned == 0) R.string.detail_category_pinned_0 else R.string.detail_category_pinned_1)
            val lB = ItemTagLayoutBinding.inflate(LayoutInflater.from(mContext), binding.tagLayout, true)
            addHeaderTag(lB.headerLayout, header)
            addContentTag(lB.contentLayout, header, listOf(it.name))
        }
    }

    fun setTitle(s: String) {
        binding.title.text = s
    }

    private fun redrawTagView(s: String) {
        binding.tagLayout.removeAllViews()

        parseTagsToMap(s).entries.sortedBy {
            it.key
        }.forEach { entry ->
            val lB = ItemTagLayoutBinding.inflate(LayoutInflater.from(mContext), binding.tagLayout, true)
            addHeaderTag(lB.headerLayout, entry.key)
            addContentTag(lB.contentLayout, entry.key, entry.value)
        }
    }

    private fun addHeaderTag(headerLayout: FlexboxLayout, s: String) {
        val tB = ItemTagBinding.inflate(LayoutInflater.from(mContext), headerLayout, true)
        tB.textView.apply {
            text = s
            if (s.lowercase() == "artist") setTextColor(Color.parseColor("#22a7f0"))
            if (s.lowercase() == "group") setTextColor(Color.parseColor("#36d7b7"))
        }
    }

    @SuppressLint("InflateParams")
    private fun addContentTag(contentLayout: FlexboxLayout, header: String, list: List<String>) {
        list.forEach { s ->
            val tB = ItemTagBinding.inflate(LayoutInflater.from(mContext), contentLayout, true)
            tB.textView.apply {
                text = if (header == "date_added") timeStamp2String(s.toLong()) else s
                isEnabled = header != "date_added"
                setBackgroundResource(R.drawable.bg_tag_content)
                setOnClickListener {
                    mListener?.onTagSelected(header, s)
                }
            }
        }
    }

    private fun timeStamp2String(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time * 1000L
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val format = "%04d/%02d/%02d"
        return String.format(format, year, month, day)
    }


    private fun parseTagsToMap(tags: String): Map<String, ArrayList<String>> {
        val result = mutableMapOf<String, ArrayList<String>>()
        val regex = Regex(",\\s*")

        tags.split(regex).forEach {
            val itemArr = it.split(Regex(":"), 2)
            val key = if (itemArr.size == 2) itemArr[0] else "未知"
            val value = if (itemArr.size == 2) itemArr[1] else it

            val tL = getOrNull { result[key] } ?: arrayListOf()
            tL.add(value)
            result[key] = tL
        }

        return result
    }

    fun setOnTagSelectedListener(func: (header: String, content: String) -> Unit) {
        mListener = object : OnTagSelectedListener {
            override fun onTagSelected(header: String, content: String) {
                func.invoke(header, content)
            }
        }
    }

    interface OnTagSelectedListener {
        fun onTagSelected(header: String, content: String)
    }



}

