package sashiro.com.trimmingview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import sashiro.com.trimmingview.ext.*
import sashiro.com.trimmingview.model.DragMode
import sashiro.com.trimmingview.model.TrimmingResult
import sashiro.com.trimmingview.model.TrimmingViewConfig
import kotlin.properties.Delegates

class TrimmingView(context: Context, attributeSet: AttributeSet?) : DragView(context, attributeSet) {
    constructor(context: Context) : this(context, null)

    // private filed
    private val trimBorderPaint = Paint()
    private val trimBgPaint = Paint()
    private val trimBorderPath = Path()
    private val trimBgPath = Path()

    private var needCalTriRectF = false
    // attrs
    override var config: TrimmingViewConfig by Delegates.observable(TrimmingViewConfig.Builder(context).build()) { _, _, newValue ->
        needCalImg = true
        applyColor()
        calStandardRectF()
        setPathByRectF(standardRectF)
        requestLayout()
    }

    override fun onGlobalLayout() {
        if (needCalTriRectF) {
            if (centerPointF.isEmpty)
                setCenter(widthF / 2, heightF / 2)

            calStandardRectF()
            setPathByRectF(standardRectF)

            needCalTriRectF = false
        }


        // init Img
        super.onGlobalLayout()
    }

    init {
        initAttrs(attributeSet)

        // init paint
        // border paint
        trimBorderPaint.apply {
            isAntiAlias = true
            color = config.borderColor
            style = Paint.Style.STROKE
            strokeWidth = config.borderWidth
        }
        // bg paint
        trimBgPaint.apply {
            isAntiAlias = true
            color = config.backgroundColor
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    // override method
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (config.showBackground)
            canvas.drawPath(trimBgPath, trimBgPaint)
        canvas.drawPath(trimBorderPath, trimBorderPaint)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        needCalTriRectF = true
        super.setImageDrawable(drawable)
    }

    // private method
    private fun initAttrs(attributeSet: AttributeSet?) {
        attributeSet?.let {
            val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TrimmingView)
            try {
                (0..typedArray.indexCount).map { i ->
                    typedArray.getIndex(i)
                }.forEach { index ->
                    when (index) {
                        R.styleable.TrimmingView_trimBorderColor -> config.borderColor = typedArray.getColor(index, getColor(R.color.black))
                        R.styleable.TrimmingView_trimBgColor -> config.backgroundColor = typedArray.getColor(index, getColor(R.color.trans_black))
                        R.styleable.TrimmingView_showTrimBg -> config.showBackground = typedArray.getBoolean(index, false)
                        R.styleable.TrimmingView_minPadding -> config.minPadding = typedArray.getDimensionPixelSize(index, resources.getDimensionPixelSize(R.dimen.min_padding_default)).toFloat()
                        R.styleable.TrimmingView_trimBorderWidth -> config.borderWidth = typedArray.getDimensionPixelSize(index, resources.getDimensionPixelSize(R.dimen.border_width_default)).toFloat()
                        R.styleable.TrimmingView_ratio -> config.ratio = typedArray.getFloat(index, 1f)
                        R.styleable.TrimmingView_dragMode -> config.dragMode = when (typedArray.getInt(index, 0)) {
                            1 -> DragMode.OverDrag
                            2 -> DragMode.Disabled
                            else -> DragMode.Default
                        }
                    }
                }
            } finally {
                typedArray.recycle()
            }
        }
    }

    private fun applyColor() {
        trimBorderPaint.apply {
            color = config.borderColor
            strokeWidth = config.borderWidth
        }
        // bg paint
        trimBgPaint.apply {
            color = config.backgroundColor
        }
    }

    private fun calStandardRectF() {
        // find standard line
        val isWidthStandard = when (rectFHasRotated) {
            false -> ((widthF - 2 * config.minPadding) / config.ratio) <= heightF
            true -> ((widthF - 2 * config.minPadding) * config.ratio) <= heightF
        }
        val borderStandardLine = when {
            (isWidthStandard && !rectFHasRotated) ||
                    (isWidthStandard && rectFHasRotated) -> widthF - 2 * config.minPadding
            else -> heightF - 2 * config.minPadding
        }
        val otherLine = when {
            (isWidthStandard && !rectFHasRotated) ||
                    (!isWidthStandard && rectFHasRotated) -> borderStandardLine / config.ratio
            else -> borderStandardLine * config.ratio
        }
        val left = when {
            isWidthStandard -> config.minPadding
            else -> (widthF - otherLine) / 2
        }
        val right = when {
            isWidthStandard -> borderStandardLine + config.minPadding
            else -> left + otherLine
        }
        val top = when {
            isWidthStandard -> (heightF - otherLine) / 2
            else -> config.minPadding
        }
        val bottom = when {
            isWidthStandard -> top + otherLine
            else -> borderStandardLine + config.minPadding
        }
        standardRectF.apply {
            this.left = left
            this.right = right
            this.top = top
            this.bottom = bottom
        }
    }

    private fun setPathByRectF(rectF: RectF) {
        val squarePoint = SquarePoint(
                PointF(rectF.left, rectF.top),
                PointF(rectF.right, rectF.top),
                PointF(rectF.right, rectF.bottom),
                PointF(rectF.left, rectF.bottom))
        setPathBySquarePoint(squarePoint)
    }

    private fun setPathBySquarePoint(squarePoint: SquarePoint) {
        trimBorderPath.apply {
            reset()
            moveTo(squarePoint.ltPoint.x, squarePoint.ltPoint.y)
            lineTo(squarePoint.rtPoint.x, squarePoint.rtPoint.y)
            lineTo(squarePoint.rbPoint.x, squarePoint.rbPoint.y)
            lineTo(squarePoint.lbPoint.x, squarePoint.lbPoint.y)
            close()
        }
        trimBgPath.apply {
            reset()
            moveTo(0f, 0f)
            lineTo(widthF, 0f)
            lineTo(widthF, heightF)
            lineTo(0f, heightF)
            close()
            fillType = Path.FillType.EVEN_ODD
            addPath(trimBorderPath)
        }
    }

    private fun rotateImg(angle: Float, isClockwise: Boolean): Float {
        if (drawable == null) return 0f
        onTrimmingViewRotate(angle, isClockwise)
        onDragViewRotated()
        return getCurrentAngle()
    }

    private fun onTrimmingViewRotate(angle: Float, isClockwise: Boolean) {
        // rotate trimPath
        rectFHasRotated = angle % 180 != 0f
        // save lastTrimResult
        triRecord.angle = when (angle >= 360f || angle <= -360f) {
            true -> 0f
            false -> angle
        }
        triRecord.lengthInfo.set(getLengthInfo(dragInfo))

        // save old standardRectF
        val oldRectF = RectF(standardRectF)
        calStandardRectF()

        // startAnim
        if (config.showAnim)
            rotateAnim(isClockwise, oldRectF, standardRectF)
        else
            setPathByRectF(standardRectF)
    }

    private fun rotateAnim(isClockwise: Boolean, startRectF: RectF, endRectF: RectF) {
        val animator = ValueAnimator()
        animator.duration = config.animDuration
        animator.setObjectValues(SquarePoint())
        animator.interpolator = LinearInterpolator()
        val startPoint = SquarePoint.transForm(startRectF)
        val endPoint = SquarePoint.transForm(endRectF)
        animator.setEvaluator { fraction, startV, endV ->
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
            SquarePoint(
                    PointF(ltX, ltY),
                    PointF(rtX, rtY),
                    PointF(rbX, rbY),
                    PointF(lbX, lbY))
        }
        animator.addUpdateListener {
            val updateRectF = it.animatedValue as SquarePoint
            setPathBySquarePoint(updateRectF)
            invalidate()
        }

        animator.start()
    }

    // inner class
    private data class SquarePoint(
            val ltPoint: PointF = PointF(),
            val rtPoint: PointF = PointF(),
            val rbPoint: PointF = PointF(),
            val lbPoint: PointF = PointF()
    ) {
        companion object {
            fun transForm(rectF: RectF): SquarePoint =
                    SquarePoint(
                            PointF(rectF.left, rectF.top),
                            PointF(rectF.right, rectF.top),
                            PointF(rectF.right, rectF.bottom),
                            PointF(rectF.left, rectF.bottom))
        }
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
        calStandardRectF()
        setPathByRectF(standardRectF)
        requestLayout()
    }

    fun changeRatio(ratio: Float) {

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