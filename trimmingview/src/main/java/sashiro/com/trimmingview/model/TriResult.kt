package sashiro.com.trimmingview.model

data class TriResult(
        val lengthInfo: LengthInfo = LengthInfo(),
        var angle: Float = 0f
) {
    fun set(src: TriResult) {
        lengthInfo.set(src.lengthInfo)
        angle = src.angle
    }

    fun isEmpty() = lengthInfo.isEmpty()

    fun hasRotated() = angle % 180 != 0f
}