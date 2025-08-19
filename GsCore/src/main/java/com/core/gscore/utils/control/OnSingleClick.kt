package com.core.gscore.utils.control

import android.view.View
import com.core.gscore.utils.extensions.postDelay

abstract class OnSingleClick(canClick: Boolean = true, var timeDelay: Long = 500L) : View.OnClickListener {
    private var canClick = true

    init {
        this.canClick = canClick
    }

    override fun onClick(view: View) {
        if (canClick) {
            canClick = false
            view.isEnabled = false
            onSingleClick(view)
            postDelay(timeDelay) {
                canClick = true
                try {
                    view.isEnabled = true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    abstract fun onSingleClick(view: View)
}