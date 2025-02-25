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

const val THRESHOLD_CLICK_TIME = 800
var lastClick = 0L

fun View.setClickSafeAll(listener: View.OnClickListener?) {
    setOnClickListener(object : View.OnClickListener {
        override fun onClick(v: View) {
            if (System.currentTimeMillis() - lastClick < THRESHOLD_CLICK_TIME) {
                return
            }
            lastClick = System.currentTimeMillis()
            listener?.onClick(v)
        }
    })
}