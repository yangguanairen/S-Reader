package com.sena.lanraragi

import androidx.fragment.app.Fragment
import com.sena.lanraragi.database.archiveData.Archive


/**
 * FileName: BaseFragment
 * Author: JiaoCan
 * Date: 2024/3/26
 */

abstract class BaseFragment : Fragment() {


    private var isLoaded = false

    protected var mNewArchiveListener: OnGenerateArchiveListener? = null

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



    fun setOnGenerateArchiveListener(func: (archive: Archive) -> Unit) {
        mNewArchiveListener = object : OnGenerateArchiveListener {
            override fun onGenerateArchive(archive: Archive) {
                func.invoke(archive)
            }
        }
    }

    protected interface OnGenerateArchiveListener {
        fun onGenerateArchive(archive: Archive)
    }
}

