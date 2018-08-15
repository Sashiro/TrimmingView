package sashiro.com.trimmingview

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.support.annotation.CallSuper
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewTreeObserver
import sashiro.com.trimmingview.ext.*
import sashiro.com.trimmingview.model.*

/** @hide */
open class DragView(context: Context, attributeSet: AttributeSet?) : AppCompatImageView(context, attributeSet),
        ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener {

    // private field
    private var maxScale = 1f
    private var standardScale = 1f
    private var lastPointerCount = 0
    private val gestureDetector = ScaleGestureDetector(context, this)
    private val photoMatrix = Matrix()
    private val matrixValues = FloatArray(9)
    private val lastTouchPointF = PointF()

    // protected field
    protected var needCalImg = true
    protected val standardRectF = RectF()
    protected val centerPointF = PointF(-1f, -1f)
    protected val dragInfo = DragInfo()
    protected val lastTriResult = TriResult()
    protected var imgWidth: Float = 0f
    protected var imgHeight: Float = 0f
    protected var rectFHasRotated = false

    // public field
    var maxScaleAs = DEFAULT_MAX_SCALE_AS

    // onGlobalLayout
    @CallSuper
    override fun onGlobalLayout() {
        if (!needCalImg || drawable == null) return

        // apply lastTriResult
        if (needApplyLastResult()) {
            dragInfo.set(transformLengthInfo(lastTriResult.lengthInfo, lastTriResult.angle))
        }

        // init standardRectF
        if (standardRectF.isEmpty)
            standardRectF.apply {
                left = 0f
                right = widthF
                top = 0f
                bottom = heightF
            }

        // init centerPointF
        if (centerPointF.isEmpty)
            setCenter(widthF / 2, heightF / 2)

        // dx dy
        val dx = centerPointF.x - drawableWF / 2
        val dy = centerPointF.y - drawableHF / 2

        // init standardScale
        standardScale = calculateStandardScale()

        // init photoMatrix
        maxScale = standardScale * maxScaleAs
        when (dragInfo.isEmpty) {
            true -> {

                photoMatrix.apply {
                    postTranslate(dx, dy)
                    postScale(standardScale, standardScale, centerPointF.x, centerPointF.y)
                }
                // save dragInfo
                dragInfo.apply {
                    lastScale = standardScale
                    lastTransX = matrixValues.getTransX(photoMatrix)
                    lastTransY = matrixValues.getTransY(photoMatrix)
                }
            }
            false -> {
                photoMatrix.apply {
                    setScale(dragInfo.lastScale, dragInfo.lastScale)
                    postTranslate(dragInfo.lastTransX, dragInfo.lastTransY)
                    postRotate(dragInfo.lastAngle, centerPointF.x, centerPointF.y)
                }
            }
        }

        imageMatrix = photoMatrix
        needCalImg = false
    }

    // init
    init {
        scaleType = ScaleType.MATRIX
        setOnTouchListener(this)
    }

    // onScale
    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean = true

    override fun onScaleEnd(p0: ScaleGestureDetector?) {

    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (drawable == null) return true
        val intentScale = detector.scaleFactor
        if (intentScale != 1f) {
            changeWithoutRotate {
                photoMatrix.postScale(intentScale, intentScale, centerPointF.x, centerPointF.y)
                val currentScale = matrixValues.getScale(photoMatrix)
                if (currentScale >= maxScale)
                    photoMatrix.postScale(maxScale / currentScale, maxScale / currentScale, centerPointF.x, centerPointF.y)
            }
            imageMatrix = photoMatrix
        }
        return true
    }

    // onTouch
    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        if (drawable == null) return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchPointF.apply {
                    x = event.getX(0)
                    y = event.getY(0)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (lastPointerCount == event.pointerCount && event.pointerCount <= 1) {
                    val dx = event.x - lastTouchPointF.x
                    val dy = event.y - lastTouchPointF.y
                    photoMatrix.postTranslate(dx, dy)
                    imageMatrix = photoMatrix
                }

                lastTouchPointF.x = event.x
                lastTouchPointF.y = event.y
                lastPointerCount = event.pointerCount
            }
            MotionEvent.ACTION_UP -> {
                if (event.pointerCount > 1) return gestureDetector.onTouchEvent(event)

                changeWithoutRotate {
                    val currentScale = matrixValues.getScale(photoMatrix)
                    when {
                        currentScale > maxScale ->
                            photoMatrix.apply {
                                setScale(dragInfo.lastScale, dragInfo.lastScale)
                                postTranslate(dragInfo.lastTransX, dragInfo.lastTransY)
                            }
                        currentScale < standardScale ->
                            resetPhotoMatrix()
                        else -> {
                            val left = matrixValues.getTransX(photoMatrix)
                            val right = left + drawableWF * currentScale
                            val top = matrixValues.getTransY(photoMatrix)
                            val bottom = top + drawableHF * currentScale

                            val currentRectF = when (dragInfo.hasRotated) {
                                true -> {
                                    val width = standardRectF.height()
                                    val height = standardRectF.width()
                                    RectF(
                                            centerPointF.x - width / 2,
                                            centerPointF.y - height / 2,
                                            centerPointF.x + width / 2,
                                            centerPointF.y + height / 2
                                    )
                                }
                                false -> standardRectF
                            }

                            val dx = when {
                                left > currentRectF.left -> currentRectF.left - left
                                right < currentRectF.right -> currentRectF.right - right
                                else -> 0f
                            }

                            val dy = when {
                                top > currentRectF.top -> currentRectF.top - top
                                bottom < currentRectF.bottom -> currentRectF.bottom - bottom
                                else -> 0f
                            }

                            photoMatrix.postTranslate(dx, dy)
                        }
                    }

                    lastTouchPointF.apply {
                        x = 0f
                        y = 0f
                    }
                    dragInfo.apply {
                        lastTransX = matrixValues.getTransX(photoMatrix)
                        lastTransY = matrixValues.getTransY(photoMatrix)
                        lastScale = matrixValues.getScale(photoMatrix)
                    }
                }

                imageMatrix = photoMatrix
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    // private method
    private fun needApplyLastResult() =
            !lastTriResult.isEmpty() && !standardRectF.isEmpty

    private fun calculateStandardScale() =
            when {
                !dragInfo.isEmpty && dragInfo.hasRotated ->
                    when {
                        drawableHF < standardRectF.width()
                                && drawableWF > standardRectF.height() ->
                            standardRectF.width() / drawableHF
                        drawableHF > standardRectF.width()
                                && drawableWF < standardRectF.height() ->
                            standardRectF.height() / drawableWF
                        (drawableHF > standardRectF.width() && drawableWF > standardRectF.height()) ||
                                (drawableHF < standardRectF.width() && drawableWF < standardRectF.height()) ->
                            Math.max(standardRectF.width() / drawableHF,
                                    standardRectF.height() / drawableWF)
                        else -> 1f
                    }
                else -> when {
                    drawableWF < standardRectF.width()
                            && drawableHF > standardRectF.height() ->
                        standardRectF.width() / drawableWF
                    drawableWF > standardRectF.width()
                            && drawableHF < standardRectF.height() ->
                        standardRectF.height() / drawableHF
                    (drawableWF > standardRectF.width() && drawableHF > standardRectF.height()) ||
                            (drawableWF < standardRectF.width() && drawableHF < standardRectF.height()) ->
                        Math.max(standardRectF.width() / drawableWF,
                                standardRectF.height() / drawableHF)
                    else -> 1f
                }
            }

    private fun changeWithoutRotate(f: () -> Unit) {
        photoMatrix.postRotate(360f - dragInfo.lastAngle, centerPointF.x, centerPointF.y)
        f.invoke()
        photoMatrix.postRotate(dragInfo.lastAngle, centerPointF.x, centerPointF.y)
    }

    private fun resetPhotoMatrix() {
        if (drawable == null) return
        photoMatrix.apply {
            setTranslate(centerPointF.x - drawableWF / 2, centerPointF.y - drawableHF / 2)
            postScale(standardScale, standardScale, centerPointF.x, centerPointF.y)
        }
    }

    private fun getLengthInfo(dragInfo: DragInfo): LengthInfo {
        val sLeft = standardRectF.left
        val sRight = standardRectF.right
        val sTop = standardRectF.top
        val sBottom = standardRectF.bottom
        val sWidth = standardRectF.width()
        val sHeight = standardRectF.height()
        val transX = dragInfo.lastTransX
        val transY = dragInfo.lastTransY
        val scale = dragInfo.lastScale
        return when (dragInfo.lastAngle % 180 == 0f) {
            true ->
                LengthInfo((sLeft - transX) / sWidth,
                        (sTop - transY) / sHeight,
                        (drawableWF * scale - sRight + transX) / sWidth,
                        (drawableHF * scale - sBottom + transY) / sHeight)
            false -> {
                val realLeft = (widthF - sHeight) / 2
                val realRight = realLeft + sHeight
                val realTop = (heightF - sWidth) / 2
                val realBottom = realTop + sWidth
                LengthInfo((realLeft - transX) / sHeight,
                        (realTop - transY) / sWidth,
                        (drawableWF * scale + transX - realRight) / sHeight,
                        (drawableHF * scale + transY - realBottom) / sWidth)
            }
        }.apply {
            if (top < 0f)
                top = 0f
            if (left < 0f)
                left = 0f
            if (right < 0f)
                right = 0f
            if (bottom < 0f)
                bottom = 0f
        }
    }

    private fun transformLengthInfo(lengthInfo: LengthInfo, angle: Float): DragInfo {
        val sLeft = standardRectF.left
        val sTop = standardRectF.top
        val sWidth = standardRectF.width()
        val sHeight = standardRectF.height()
        return when (angle % 180 == 0f) {
            true ->
                DragInfo(sLeft - lengthInfo.left * sWidth,
                        sTop - lengthInfo.top * sHeight,
                        ((lengthInfo.left + lengthInfo.right) * sWidth + sWidth) / drawableWF,
                        angle)
            false ->
                DragInfo((widthF - sHeight) / 2 - lengthInfo.left * sHeight,
                        (heightF - sWidth) / 2 - lengthInfo.top * sWidth,
                        ((lengthInfo.left + lengthInfo.right) * sHeight + sHeight) / drawableWF,
                        angle)
        }
    }

    // protect method
    protected fun setCenter(x: Float = -1f,
                            y: Float = -1f) =
            centerPointF.apply {
                this.x = x
                this.y = y
            }

    // override method
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    @CallSuper
    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        needCalImg = true
        requestLayout()
    }

    // public method
    @CallSuper
    open fun rotateImg(angle: Float): Float {
        if (drawable == null) return 0f
        // save lastTrimResult
        lastTriResult.angle = when (angle >= 360f || angle <= -360f) {
            true -> 0f
            false -> angle
        }
        lastTriResult.lengthInfo.set(getLengthInfo(dragInfo))

        requestLayout()
        return getCurrentAngle()
    }

    fun getCurrentAngle() = dragInfo.lastAngle

    companion object {
        private const val DEFAULT_MAX_SCALE_AS = 4
    }
}