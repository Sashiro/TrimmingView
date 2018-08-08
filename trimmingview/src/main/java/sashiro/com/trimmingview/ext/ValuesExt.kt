package sashiro.com.trimmingview.ext

import android.graphics.Matrix
import android.graphics.PointF
import android.support.annotation.IntRange

val PointF.isEmpty
    get() = x == -1f && y == -1f

// matrix
fun FloatArray.getTransX(matrix: Matrix) =
        getValues(matrix, Matrix.MTRANS_X)

fun FloatArray.getTransY(matrix: Matrix) =
        getValues(matrix, Matrix.MSCALE_Y)

fun FloatArray.getScale(matrix: Matrix) =
        getValues(matrix, Matrix.MSCALE_X)

fun FloatArray.getValues(matrix: Matrix, @IntRange(from = 0, to = 8) type: Int): Float {
    matrix.getValues(this)
    return this[type]
}