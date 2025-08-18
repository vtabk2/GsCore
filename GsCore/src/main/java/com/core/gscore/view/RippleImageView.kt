package com.core.gscore.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import com.core.gscore.R

class RippleImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var imageView: AppCompatImageView? = null

    var paddingRipple: Float = 6f
        set(value) {
            val paddingNew = value.toInt()
            setPadding(paddingNew, paddingNew, paddingNew, paddingNew)
            invalidate()
        }

    var iconRippleRes: Int = 0
        set(value) {
            field = value
            imageView?.setImageResource(field)
            invalidate()
        }

    init {
        isClickable = true
        isFocusable = true

        if (imageView == null) {
            imageView = AppCompatImageView(context)
            imageView?.let {
                it.scaleX = resources.getInteger(R.integer.locale_mirror_flip).toFloat()
                addView(it)
            }
        }

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.RippleImageView) {
                paddingRipple = getDimension(R.styleable.RippleImageView_riv_padding_ripple, paddingRipple)
                iconRippleRes = getResourceId(R.styleable.RippleImageView_riv_icon_ripple, iconRippleRes)
            }
        }

        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
        setBackgroundResource(typedValue.resourceId)
    }

    override fun setEnabled(enabled: Boolean) {
        imageView?.isEnabled = enabled
        super.setEnabled(enabled)
    }
}