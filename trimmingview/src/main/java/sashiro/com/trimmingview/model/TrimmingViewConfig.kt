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
        var ratio: Float,
        var showAnim: Boolean,
        var animDuration: Long,
        var dragMode: DragMode,
        var maxScale: Float,
        var canTrimFrameDrag: Boolean,
        var trimFrameAnimDuration: Long
) {
    fun set(src: TrimmingViewConfig) {
        this.borderColor = src.borderColor
        this.backgroundColor = src.backgroundColor
        this.borderWidth = src.borderWidth
        this.showBackground = src.showBackground
        this.minPadding = src.minPadding
        this.ratio = src.ratio
        this.showAnim = src.showAnim
        this.animDuration = src.animDuration
        this.dragMode = src.dragMode
        this.maxScale = src.maxScale
        this.canTrimFrameDrag = src.canTrimFrameDrag
        this.trimFrameAnimDuration = src.trimFrameAnimDuration
    }


    class Builder(context: Context) {
        private var borderColor: Int
        private var backgroundColor: Int
        private var borderWidth: Float
        private var showBackground: Boolean
        private var minPadding: Float
        private var ratio: Float
        private var showAnim: Boolean
        private var animDuration: Long
        private var dragMode: DragMode
        private var maxScale: Float
        private var canTrimFrameDrag: Boolean
        private var trimFrameAnimDuration: Long

        init {
            borderColor = context.getCompColor(R.color.black)
            backgroundColor = context.getCompColor(R.color.trans_black)
            borderWidth = context.resources.getDimensionPixelSize(R.dimen.border_width_default).toFloat()
            showBackground = false
            minPadding = context.resources.getDimensionPixelSize(R.dimen.min_padding_default).toFloat()
            ratio = 1f
            showAnim = false
            animDuration = DEFAULT_ANIM_DURATION
            dragMode = DragMode.Default
            maxScale = DEFAULT_MAX_SCALE
            canTrimFrameDrag = false
            trimFrameAnimDuration = DEFAULT_FRAME_ANIM_DURATION
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

        fun showAnim(showAnim: Boolean): Builder {
            this.showAnim = showAnim
            return this
        }

        fun setAnimDuration(animDuration: Long): Builder {
            this.animDuration = animDuration
            return this
        }

        fun setDragMode(dragMode: DragMode): Builder {
            this.dragMode = dragMode
            return this
        }

        fun maxScale(maxScale: Float): Builder {
            this.maxScale = maxScale
            return this
        }

        fun canTrimFrameDrag(canTrimFrameDrag: Boolean): Builder {
            this.canTrimFrameDrag = canTrimFrameDrag
            return this
        }

        fun setTrimFrameAnimDuration(trimFrameAnimDuration: Long): Builder {
            this.trimFrameAnimDuration = trimFrameAnimDuration
            return this
        }

        fun build(): TrimmingViewConfig =
                TrimmingViewConfig(borderColor,
                        backgroundColor,
                        borderWidth,
                        showBackground,
                        minPadding,
                        ratio,
                        showAnim,
                        animDuration,
                        dragMode,
                        maxScale,
                        canTrimFrameDrag,
                        trimFrameAnimDuration)

    }

    companion object {
        const val DEFAULT_MAX_SCALE = 5f
        const val DEFAULT_ANIM_DURATION = 250L
        const val DEFAULT_FRAME_ANIM_DURATION = 150L
    }

}