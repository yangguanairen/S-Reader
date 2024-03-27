package com.sena.lanraragi

import androidx.fragment.app.Fragment


/**
 * FileName: BaseFragment
 * Author: JiaoCan
 * Date: 2024/3/26
 */

abstract class BaseFragment : Fragment() {


    private var isLoaded = false

    protected var mListener: ResumeListener? = null


    override fun onResume() {
        super.onResume()
        if (!isLoaded) {
            isLoaded = true
            lazyLoad()
        }
        isLoaded = true
    }

    open fun lazyLoad() {

    }

    fun setOnResumeListener(func: (sList: List<String>) -> Unit) {
        mListener = object : ResumeListener {
            override fun onResumeListener(sList: List<String>) {
                func.invoke(sList)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
    }


    protected interface ResumeListener {
        fun onResumeListener(sList: List<String>)
    }

}

