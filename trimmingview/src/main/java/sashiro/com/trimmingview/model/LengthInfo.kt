package sashiro.com.trimmingview.model

data class LengthInfo(
        var left: Float = 0f,
        var top: Float = 0f,
        var right: Float = 0f,
        var bottom: Float = 0f
) {
    fun set(src: LengthInfo) {
        left = src.left
        top = src.top
        right = src.right
        bottom = src.bottom
    }

    fun isEmpty() = left == 0f
            && top == 0f
            && right == 0f
            && bottom == 0f
}