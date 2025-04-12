package com.core.gscore.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.core.gscore.R

class AspectRatioLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_RATIO = -1f
    }

    var widthToHeight: Float = DEFAULT_RATIO
        set(value) {
            if (field != value) {
                field = value
                if (value != DEFAULT_RATIO) heightToWidth = DEFAULT_RATIO
                requestLayout()
            }
        }

    var heightToWidth: Float = DEFAULT_RATIO
        set(value) {
            if (field != value) {
                field = value
                if (value != DEFAULT_RATIO) widthToHeight = DEFAULT_RATIO
                requestLayout()
            }
        }

    init {
        attrs?.let {
            context.withStyledAttributes(attrs, R.styleable.AspectRatioLayout) {
                widthToHeight = getFloat(R.styleable.AspectRatioLayout_widthToHeight, DEFAULT_RATIO)
                heightToWidth = getFloat(R.styleable.AspectRatioLayout_heightToWidth, DEFAULT_RATIO)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when {
            widthToHeight != DEFAULT_RATIO -> {
                val desiredHeight = MeasureSpec.getSize(heightMeasureSpec)
                val desiredWidth = (desiredHeight * widthToHeight).toInt()
                setMeasuredDimension(desiredWidth, desiredHeight)
            }

            heightToWidth != DEFAULT_RATIO -> {
                val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)
                val desiredHeight = (desiredWidth * heightToWidth).toInt()
                setMeasuredDimension(desiredWidth, desiredHeight)
            }

            else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}