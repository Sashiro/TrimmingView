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
import sashiro.com.trimmingview.model.DragInfo
import sashiro.com.trimmingview.model.TriResult
import sashiro.com.trimmingview.model.hasRotated
import sashiro.com.trimmingview.model.isEmpty

/** @hide */
open class DragView(context: Context, attributeSet: AttributeSet?) : AppCompatImageView(context, attributeSet),
        ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener {

    constructor(context: Context) : this(context, null)

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

    // public field
    var maxScaleAs = DEFAULT_MAX_SCALE_AS

    // onGlobalLayout
    @CallSuper
    override fun onGlobalLayout() {
        if (!needCalImg || drawable == null) return

        // apply lastTriResult
        if (needApplyLastResult())
            applyLastResult()

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

    override fun onScale(p0: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return true
    }

    // private method
    private fun needApplyLastResult() =
            !lastTriResult.isEmpty() && !standardRectF.isEmpty
                    && imgWidth > 0f && imgHeight > 0f

    private fun applyLastResult() {
        val realStandardRectF = when (lastTriResult.isRotated()) {
            true -> getRotatedRectF()
            false -> standardRectF
        }

        val realScale = realStandardRectF.width() / lastTriResult.absoluteTriRectF.width()
        val lastScale = imgWidth * realScale / drawableWF
        val transX = (widthF - realStandardRectF.width()) / 2 - lastTriResult.absoluteTriRectF.left * realScale
        val transY = (heightF - realStandardRectF.height()) / 2 - lastTriResult.absoluteTriRectF.top * realScale

        dragInfo.apply {
            this.lastScale = lastScale
            this.lastTransX = transX
            this.lastTransY = transY
            this.lastAngle = lastTriResult.angle
        }
    }


    private fun calculateStandardScale() =
            when {
                dragInfo.isEmpty && dragInfo.hasRotated ->
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

    // protect method
    protected fun getRotatedRectF(): RectF {
        val left = (widthF - standardRectF.height()) / 2
        val right = left + standardRectF.height()
        val top = (heightF - standardRectF.width()) / 2
        val bottom = top + standardRectF.width()
        return RectF(left, top, right, bottom)
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

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        needCalImg = true
        requestLayout()
    }

    // public method
    fun setCenter(x: Float = -1f,
                  y: Float = -1f) =
            centerPointF.apply {
                this.x = x
                this.y = y
            }

    fun setCenter(pointF: PointF) =
            setCenter(pointF.x, pointF.y)

    companion object {
        private const val DEFAULT_MAX_SCALE_AS = 4
    }
}