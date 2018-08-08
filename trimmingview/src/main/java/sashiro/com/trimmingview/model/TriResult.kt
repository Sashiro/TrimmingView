package sashiro.com.trimmingview.model

import android.graphics.RectF

data class TriResult(
        var imgPath: String = "",
        val absoluteTriRectF: RectF = RectF(),
        var imgWidth: Float = 0f,
        var imgHeight: Float = 0f,
        var rotate: Float = 0f
) {
    fun set(src: TriResult) {
        imgPath = src.imgPath
        absoluteTriRectF.set(src.absoluteTriRectF)
        imgWidth = src.imgWidth
        imgHeight = src.imgHeight
        rotate = src.rotate
    }
}