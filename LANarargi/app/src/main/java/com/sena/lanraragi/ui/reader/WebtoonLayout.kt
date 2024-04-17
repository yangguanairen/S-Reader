package com.sena.lanraragi.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * FileName: WebtoonLayout
 * Author: JiaoCan
 * Date: 2024/4/16
 */

@SuppressLint("ViewConstructor", "InflateParams")
class WebtoonLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private val lm = LinearLayoutManager(context)
    private val mAdapter = WebtoonAdapter()

    init {
        layoutManager = lm
        adapter = mAdapter
    }


//    fun setOnImageClickListener(func: (a: WebtoonAdapter, v: View, p: Int) -> Unit)  {
//        mOnClickListener = object : WebtoonAdapter.OnClickListener {
//            override fun onClick(a: WebtoonAdapter, v: View, p: Int) {
//                func.invoke(a, v, p)
//            }
//        }
//    }
//
//    fun setOnImageLongClickListener(func: (a: WebtoonAdapter, v: View, p: Int) -> Boolean)  {
//        mOnLongClickListener = object : WebtoonAdapter.OnLongClickListener {
//            override fun onLongClick(a: WebtoonAdapter, v: View, p: Int): Boolean {
//                return func.invoke(a, v, p)
//            }
//        }
//    }

}

