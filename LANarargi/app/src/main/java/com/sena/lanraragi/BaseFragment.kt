package com.sena.lanraragi

import androidx.fragment.app.Fragment


/**
 * FileName: BaseFragment
 * Author: JiaoCan
 * Date: 2024/3/26
 */

abstract class BaseFragment : Fragment() {


    private var isLoaded = false

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


    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
    }
}

