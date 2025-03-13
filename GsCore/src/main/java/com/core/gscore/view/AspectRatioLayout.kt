package com.core.gscore.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.core.gscore.R
import androidx.core.content.withStyledAttributes

class AspectRatioLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    var widthToHeight: Float = -1f
        set(value) {
            field = value
            invalidate()
        }

    var heightToWidth: Float = -1f
        set(value) {
            field = value
            invalidate()
        }

    init {
        attrs?.let {
            context.withStyledAttributes(attrs, R.styleable.AspectRatioLayout) {
                widthToHeight = getFloat(R.styleable.AspectRatioLayout_widthToHeight, widthToHeight)
                heightToWidth = getFloat(R.styleable.AspectRatioLayout_heightToWidth, heightToWidth)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth: Int
        val desiredHeight: Int
        if (widthToHeight != -1f) {
            desiredHeight = MeasureSpec.getSize(heightMeasureSpec)
            desiredWidth = (desiredHeight * widthToHeight).toInt()
        } else if (heightToWidth != -1f) {
            desiredWidth = MeasureSpec.getSize(widthMeasureSpec)
            desiredHeight = (desiredWidth * heightToWidth).toInt()
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val desiredWidthMeasureSpec = MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.EXACTLY)
        val desiredHeightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY)
        super.onMeasure(desiredWidthMeasureSpec, desiredHeightMeasureSpec)
    }
}