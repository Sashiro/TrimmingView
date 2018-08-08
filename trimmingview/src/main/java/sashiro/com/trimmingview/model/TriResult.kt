package sashiro.com.trimmingview.model

import android.graphics.RectF

data class TriResult(
        val absoluteTriRectF: RectF = RectF(),
        var angle: Float = 0f
) {
    fun set(src: TriResult) {
        absoluteTriRectF.set(src.absoluteTriRectF)
        angle = src.angle
    }

    fun isEmpty() = absoluteTriRectF.isEmpty

    fun isRotated() = angle % 180 != 0f
}