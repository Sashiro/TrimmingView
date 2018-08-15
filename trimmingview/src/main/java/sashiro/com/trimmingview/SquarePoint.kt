package sashiro.com.trimmingview

import android.graphics.PointF

data class SquarePoint(
        val ltPoint: PointF = PointF(),
        val rtPoint: PointF = PointF(),
        val rbPoint: PointF = PointF(),
        val lbPoint: PointF = PointF()
)