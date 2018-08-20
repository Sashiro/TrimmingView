package sashiro.com.trimmingview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import sashiro.com.trimmingview.model.TrimmingResult
import sashiro.com.trimmingview.model.TrimmingViewConfig
import kotlin.properties.Delegates

class TrimmingView(context: Context, attributeSet: AttributeSet?) : RectFView(context, attributeSet) {
    constructor(context: Context) : this(context, null)

    // private method
    private fun rotateImg(angle: Float, isClockwise: Boolean) {
        if (drawable == null) return
        onRotate(angle, isClockwise)
    }

    private fun onRotate(angle: Float, isClockwise: Boolean) {
        rectFHasRotated = angle % 180 != 0f
        // save lastTrimResult
        triRecord.angle = when (angle >= 360f || angle <= -360f) {
            true -> 0f
            false -> angle
        }
        triRecord.lengthInfo.set(getLengthInfo(dragInfo))

        // save old standardRectF
        val oldRectF = RectF(standardRectF)
        standardRectF.set(calStandardRectF())

        // save old dragInfo
        val oldDragInfo = DragInfo(dragInfo)
        dragInfo.set(transformLengthInfo(triRecord.lengthInfo, triRecord.angle))

        // startAnim
        if (config.showAnim)
            rotateAnim(isClockwise, oldRectF, standardRectF, oldDragInfo, dragInfo)
        else {
            // change rectF
            setPathByRectF(standardRectF)
            // change img
            photoMatrix.apply {
                setScale(dragInfo.lastScale, dragInfo.lastScale)
                postTranslate(dragInfo.lastTransX, dragInfo.lastTransY)
                postRotate(dragInfo.lastAngle, centerPointF.x, centerPointF.y)
            }
            imageMatrix = photoMatrix
        }
    }

    private fun rotateAnim(isClockwise: Boolean,
                           startRectF: RectF, endRectF: RectF,
                           startDragInfo: DragInfo, endDragInfo: DragInfo) {
        val animator = ValueAnimator()
        animator.duration = config.animDuration
        animator.setObjectValues(SquarePoint() to DragInfo())
        animator.interpolator = LinearInterpolator()
        val startPoint = SquarePoint.transForm(startRectF)
        val endPoint = SquarePoint.transForm(endRectF)
        animator.setEvaluator { fraction, startV, endV ->
            val currentSquarePoint = getCurrentSquarePoint(isClockwise, fraction, startPoint, endPoint)
            val currentDragInfo = getCurrentDragInfo(fraction, startDragInfo, endDragInfo)
            currentSquarePoint to currentDragInfo
        }
        animator.addUpdateListener {
            val updateRectF = it.animatedValue as Pair<SquarePoint, DragInfo>
            // rotate rectF
            setPathBySquarePoint(updateRectF.first)
            // rotate img
            photoMatrix.apply {
                setScale(updateRectF.second.lastScale, updateRectF.second.lastScale)
                postTranslate(updateRectF.second.lastTransX, updateRectF.second.lastTransY)
                postRotate(updateRectF.second.lastAngle, centerPointF.x, centerPointF.y)
            }
            imageMatrix = photoMatrix
        }
        animator.start()
    }

    private fun getCurrentSquarePoint(isClockwise: Boolean, fraction: Float,
                                      startPoint: SquarePoint, endPoint: SquarePoint): SquarePoint {
        val ltX = when (isClockwise) {
            true -> endPoint.rtPoint.x * fraction + (1 - fraction) * startPoint.ltPoint.x
            false -> endPoint.lbPoint.x * fraction + (1 - fraction) * startPoint.ltPoint.x
        }
        val ltY = when (isClockwise) {
            true -> endPoint.rtPoint.y * fraction + (1 - fraction) * startPoint.ltPoint.y
            false -> endPoint.lbPoint.y * fraction + (1 - fraction) * startPoint.ltPoint.y
        }

        val rtX = when (isClockwise) {
            true -> endPoint.rbPoint.x * fraction + (1 - fraction) * startPoint.rtPoint.x
            false -> endPoint.ltPoint.x * fraction + (1 - fraction) * startPoint.rtPoint.x
        }
        val rtY = when (isClockwise) {
            true -> endPoint.rbPoint.y * fraction + (1 - fraction) * startPoint.rtPoint.y
            false -> endPoint.ltPoint.y * fraction + (1 - fraction) * startPoint.rtPoint.y
        }

        val lbX = when (isClockwise) {
            true -> endPoint.ltPoint.x * fraction + (1 - fraction) * startPoint.lbPoint.x
            false -> endPoint.rbPoint.x * fraction + (1 - fraction) * startPoint.lbPoint.x
        }
        val lbY = when (isClockwise) {
            true -> endPoint.ltPoint.y * fraction + (1 - fraction) * startPoint.lbPoint.y
            false -> endPoint.rbPoint.y * fraction + (1 - fraction) * startPoint.lbPoint.y
        }

        val rbX = when (isClockwise) {
            true -> endPoint.lbPoint.x * fraction + (1 - fraction) * startPoint.rbPoint.x
            false -> endPoint.rtPoint.x * fraction + (1 - fraction) * startPoint.rbPoint.x
        }
        val rbY = when (isClockwise) {
            true -> endPoint.lbPoint.y * fraction + (1 - fraction) * startPoint.rbPoint.y
            false -> endPoint.rtPoint.y * fraction + (1 - fraction) * startPoint.rbPoint.y
        }
        return SquarePoint(
                PointF(ltX, ltY),
                PointF(rtX, rtY),
                PointF(rbX, rbY),
                PointF(lbX, lbY))
    }

    private fun getCurrentDragInfo(fraction: Float,
                                   startDragInfo: DragInfo, endDragInfo: DragInfo): DragInfo {
        val scale = endDragInfo.lastScale * fraction + (1 - fraction) * startDragInfo.lastScale
        val transX = endDragInfo.lastTransX * fraction + (1 - fraction) * startDragInfo.lastTransX
        val transY = endDragInfo.lastTransY * fraction + (1 - fraction) * startDragInfo.lastTransY
        val angle = when {
            endDragInfo.lastAngle == 0f && startDragInfo.lastAngle == -270f ->
                -360f * fraction + (1 - fraction) * startDragInfo.lastAngle
            endDragInfo.lastAngle == 0f && startDragInfo.lastAngle == 270f ->
                360f * fraction + (1 - fraction) * startDragInfo.lastAngle
            else -> endDragInfo.lastAngle * fraction + (1 - fraction) * startDragInfo.lastAngle
        }
        return DragInfo(transX, transY, scale, angle)
    }

    // public method
    fun getResult(imgWidth: Int, imgHeight: Int): TrimmingResult {
        val lengthInfo = getLengthInfo(dragInfo)
        val sWidth = imgWidth / (1 + lengthInfo.left + lengthInfo.right)
        val sHeight = imgHeight / (1 + lengthInfo.top + lengthInfo.bottom)
        val left = Math.round(lengthInfo.left * sWidth).let {
            when {
                it < 0 -> 0
                else -> it
            }
        }
        val right = Math.round(imgWidth - lengthInfo.right * sWidth).let {
            when {
                it > imgWidth -> imgWidth
                else -> it
            }
        }
        val top = Math.round(lengthInfo.top * sHeight).let {
            when {
                it < 0 -> 0
                else -> it
            }
        }
        val bottom = Math.round(imgHeight - lengthInfo.bottom * sHeight).let {
            when {
                it > imgHeight -> imgHeight
                else -> it
            }
        }
        return TrimmingResult(Rect(left, top, right, bottom), getCurrentAngle())
    }

    fun setResult(imgWidth: Int, imgHeight: Int, trimmingResult: TrimmingResult) {
        triRecord.clear()
        dragInfo.clear()
        config.ratio = trimmingResult.trimmingRect.width() / trimmingResult.trimmingRect.height().toFloat()
        needCalImg = true


        val left = trimmingResult.trimmingRect.left / trimmingResult.trimmingRect.width().toFloat()
        val right = (imgWidth - trimmingResult.trimmingRect.right) / trimmingResult.trimmingRect.width().toFloat()
        val top = trimmingResult.trimmingRect.top / trimmingResult.trimmingRect.height().toFloat()
        val bottom = (imgHeight - trimmingResult.trimmingRect.bottom) / trimmingResult.trimmingRect.height().toFloat()

        val lengthInfo = LengthInfo(left, top, right, bottom)
        triRecord.set(TriRecord(lengthInfo, trimmingResult.angle))
        rectFHasRotated = trimmingResult.angle % 180 != 0f
        setStandardRectF()
        requestLayout()
    }

    fun reset() {
        triRecord.clear()
        dragInfo.clear()
        rectFHasRotated = false
        needCalTriRectF = true
        needCalImg = true
        requestLayout()
    }

    fun getCurrentAngle() = dragInfo.lastAngle

    fun turnClockwise() =
            rotateImg(getCurrentAngle() + 90f, true)


    fun turnAnticlockwise() =
            rotateImg(getCurrentAngle() - 90f, false)
}