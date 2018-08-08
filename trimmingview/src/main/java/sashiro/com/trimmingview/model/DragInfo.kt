package sashiro.com.trimmingview.model

data class DragInfo(
        var lastTransX: Float = -1f,
        var lastTransY: Float = -1f,
        var lastScale: Float = 0f,
        var lastAngle: Float = 0f
) {
    fun set(src: DragInfo) {
        lastTransX = src.lastTransX
        lastTransY = src.lastTransY
        lastScale = src.lastScale
        lastAngle = src.lastAngle
    }


}

// ext
val DragInfo.isEmpty
    get() = lastTransX == -1f ||
            lastTransY == -1f ||
            lastScale == 0f

val DragInfo.hasRotated
    get() = lastAngle % 180 != 0f