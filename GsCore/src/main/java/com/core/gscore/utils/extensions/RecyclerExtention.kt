package com.core.gscore.utils.extensions

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

fun RecyclerView.removeBlink(): RecyclerView {
    val animator = this.itemAnimator
    if (animator is SimpleItemAnimator) {
        animator.supportsChangeAnimations = false
    }
    return this
}