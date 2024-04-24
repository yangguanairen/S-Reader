package com.sena.lanraragi.ui.reader

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout

class TouchToggleLayout @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : RelativeLayout(context, attributeSet) {
    var enableTouch = true

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return enableTouch && super.dispatchTouchEvent(ev)
    }
}