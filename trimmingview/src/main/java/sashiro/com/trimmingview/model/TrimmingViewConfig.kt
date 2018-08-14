package sashiro.com.trimmingview.model

import android.content.Context
import sashiro.com.trimmingview.R
import sashiro.com.trimmingview.ext.getCompColor

class TrimmingViewConfig private constructor(
        var borderColor: Int,
        var backgroundColor: Int,
        var borderWidth: Float,
        var showBackground: Boolean,
        var minPadding: Float,
        var ratio: Float
) {
    fun set(src: TrimmingViewConfig) {
        this.borderColor = src.borderColor
        this.backgroundColor = src.backgroundColor
        this.borderWidth = src.borderWidth
        this.showBackground = src.showBackground
        this.minPadding = src.minPadding
        this.ratio = src.ratio
    }


    class Builder(context: Context) {
        private var borderColor: Int
        private var backgroundColor: Int
        private var borderWidth: Float
        private var showBackground: Boolean
        private var minPadding: Float
        private var ratio: Float

        init {
            borderColor = context.getCompColor(R.color.black)
            backgroundColor = context.getCompColor(R.color.trans_black)
            borderWidth = context.resources.getDimensionPixelSize(R.dimen.border_width_default).toFloat()
            showBackground = false
            minPadding = context.resources.getDimensionPixelSize(R.dimen.min_padding_default).toFloat()
            ratio = 1f
        }

        fun setBorderColor(borderColor: Int): Builder {
            this.borderColor = borderColor
            return this
        }

        fun setBorderWidth(borderWidth: Float): Builder {
            this.borderWidth = borderWidth
            return this
        }

        fun setBackgroundColor(backgroundColor: Int): Builder {
            this.backgroundColor = backgroundColor
            return this
        }

        fun isBackgroundShow(isShow: Boolean): Builder {
            this.showBackground = isShow
            return this
        }

        fun setMinPadding(minPadding: Float): Builder {
            this.minPadding = minPadding
            return this
        }

        fun setRatio(ratio: Float): Builder {
            this.ratio = ratio
            return this
        }


        fun build(): TrimmingViewConfig =
                TrimmingViewConfig(borderColor,
                        backgroundColor,
                        borderWidth,
                        showBackground,
                        minPadding,
                        ratio)

    }
}