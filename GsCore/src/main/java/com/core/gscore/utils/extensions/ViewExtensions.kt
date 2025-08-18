package com.core.gscore.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import androidx.transition.Slide
import androidx.transition.TransitionManager
import kotlin.math.hypot

fun View.invisibleIf(invisible: Boolean) = if (invisible) invisible() else visible()

fun View.visibleIf(visible: Boolean) = if (visible) visible() else gone()

fun View.visibleCircularIf(visible: Boolean) =
    if (visible) visibleCircular() else invisibleCircular()

fun View.goneIf(gone: Boolean) = visibleIf(!gone)

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visible(rootLayout: ViewGroup? = null, onlyUseSlide: Boolean = false) {
    rootLayout?.let {
        if (height == 0 || onlyUseSlide) {
            val slide = Slide(Gravity.BOTTOM)
            slide.addTarget(this)
            TransitionManager.beginDelayedTransition(it, slide)
            visibility = View.VISIBLE
        } else {
            visibility = View.VISIBLE
            val animate = TranslateAnimation(0f, 0f, this.height.toFloat(), 0f)
            animate.duration = 250L
            this.startAnimation(animate)
        }
    } ?: run {
        visibility = View.VISIBLE
    }
}

fun View.gone(
    useAnimation: Boolean = false,
    duration: Long = 250L,
    extra: Int = 0,
    callbackEnd: ((end: Boolean) -> Unit)? = null
) {
    if (useAnimation) {
        val animate = TranslateAnimation(0f, 0f, 0f, extra + this.height.toFloat())
        animate.duration = duration
        animate.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                gone()
                callbackEnd?.invoke(true)
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        })
        callbackEnd?.invoke(false)
        this.startAnimation(animate)
    } else {
        visibility = View.GONE
        callbackEnd?.invoke(true)
    }
}

fun View.visibleIf(visible: Boolean, invisible: Boolean) {
    if (visible) {
        visible()
    } else {
        if (invisible) {
            invisible()
        } else {
            gone()
        }
    }
}

fun View.visibleCircular(cx: Int = width / 2, cy: Int = height / 2) {
    try {
        // get the final radius for the clipping circle
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        // create the animator for this view (the start radius is zero)
        val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius)
        // make the view visible and start the animation
        visible()
        anim.start()
    } catch (e: Exception) {
        e.printStackTrace()
        visible()
    }
}

fun View.invisibleCircular(cx: Int = width / 2, cy: Int = height / 2) {
    try {
        // get the initial radius for the clipping circle
        val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        // create the animation (the final radius is zero)
        val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, initialRadius, 0f)

        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                invisible()
            }
        })

        // start the animation
        anim.start()
    } catch (e: Exception) {
        e.printStackTrace()
        invisible()
    }
}

fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback()
        }
    })
}

private const val THRESHOLD_CLICK_TIME = 600L
private val lastClickMap = mutableMapOf<Int, Long>()
private var lastCleanupTime = 0L
private const val CLEANUP_INTERVAL = 5000L

fun View.setClickSafeAll(action: (v: View) -> Unit) {
    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        val viewId = view.id

        // Kiểm tra thời gian click trước đó
        val lastClick = lastClickMap[viewId] ?: 0L
        if (currentTime - lastClick <= THRESHOLD_CLICK_TIME) {
            return@setOnClickListener
        }

        // Cập nhật thời gian click mới
        lastClickMap[viewId] = currentTime

        // Thực hiện action
        action(view)

        // Dọn dẹp định kỳ (không chạy mỗi lần click)
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL) {
            lastCleanupTime = currentTime
            cleanupOldEntries(currentTime)
        }
    }
}

private fun cleanupOldEntries(currentTime: Long) {
    val iterator = lastClickMap.iterator()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        if (currentTime - entry.value > THRESHOLD_CLICK_TIME * 2) {
            iterator.remove()
        }
    }
}