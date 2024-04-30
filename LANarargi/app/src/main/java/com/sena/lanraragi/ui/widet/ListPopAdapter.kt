package com.sena.lanraragi.ui.widet

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import com.sena.lanraragi.database.statsData.Stats
import com.sena.lanraragi.databinding.ItemSearchRelatedBinding


/**
 * FileName: TestAdapter
 * Author: JiaoCan
 * Date: 2024/4/28
 */

class ListPopAdapter(context: Context, list: List<Stats>, inputText: String) : ListAdapter {

    private val mContext = context
    private val mList = list
    private val mInputText = inputText

    fun getList() = mList

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val binding = ItemSearchRelatedBinding.inflate(LayoutInflater.from(mContext), parent, false)

        val item = mList[position]
        val splicingText = item.splicingText
        binding.content.apply {

            val startIndex = splicingText.indexOf(mInputText)
            if (startIndex > -1) {
                val endIndex = startIndex + mInputText.length
                val ss = SpannableString(splicingText).apply {
                    val fSpan = ForegroundColorSpan(Color.parseColor("#000000"))
                    setSpan(fSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val bSpan = BackgroundColorSpan(Color.parseColor("#22a7f0"))
                    setSpan(bSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                text = ss
            } else {
                text = splicingText
            }
        }
        return binding.root
    }


    override fun registerDataSetObserver(observer: DataSetObserver?) {
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
    }

    override fun getCount(): Int = mList.size

    override fun getItem(position: Int): Any = mList[position]

    override fun getItemId(position: Int): Long = -1

    override fun hasStableIds(): Boolean = false

    override fun getItemViewType(position: Int): Int = position

    override fun getViewTypeCount(): Int = 1

    override fun isEmpty(): Boolean = mList.isEmpty()

    override fun areAllItemsEnabled(): Boolean = true

    override fun isEnabled(position: Int): Boolean = true
}

