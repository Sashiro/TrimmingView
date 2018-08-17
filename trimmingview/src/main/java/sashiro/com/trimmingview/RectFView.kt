package sashiro.com.trimmingview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import sashiro.com.trimmingview.ext.*
import sashiro.com.trimmingview.model.DragMode
import sashiro.com.trimmingview.model.TrimmingViewConfig
import kotlin.properties.Delegates

/** @hide */
abstract class RectFView(context: Context, attributeSet: AttributeSet?) : DragView(context, attributeSet) {
    constructor(context: Context) : this(context, null)

    override var config: TrimmingViewConfig by Delegates.observable(TrimmingViewConfig.Builder(context).build()) { _, _, newValue ->
        needCalImg = true
        applyColor()
        calStandardRectF()
        setPathByRectF(standardRectF)
        requestLayout()
    }

    // private filed
    private val trimBorderPaint = Paint()
    private val trimBgPaint = Paint()
    private val trimBorderPath = Path()
    private val trimBgPath = Path()
    private var dragType: DragType = DragType.Image
    private val changeRectF: RectF = RectF()

    protected var needCalTriRectF = false

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

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragType = checkDragType(event, standardRectF)
                changeRectF.set(standardRectF)
                if (dragType != DragType.Image) {
                    lastTouchPointF.set(event.x, event.y)
                    return true
                } else return super.onTouch(view, event)
            }
            else -> {
                if (dragType != DragType.Image) {
                    when (event.action) {
                        MotionEvent.ACTION_MOVE -> {
                            val dx = event.x - lastTouchPointF.x
                            val dy = event.y - lastTouchPointF.y
                            val currentSquarePoint = SquarePoint.transForm(standardRectF)
                            when (dragType) {
                                DragType.LTPoint -> {
                                    currentSquarePoint.ltPoint.apply {
                                        x += dx
                                        y += dy
                                    }
                                    currentSquarePoint.lbPoint.x += dx
                                    currentSquarePoint.rtPoint.y += dy
                                }
                                DragType.RTPoint -> {
                                    currentSquarePoint.rtPoint.apply {
                                        x += dx
                                        y += dy
                                    }
                                    currentSquarePoint.rbPoint.x += dx
                                    currentSquarePoint.ltPoint.y += dy
                                }
                                DragType.RBPoint -> {
                                    currentSquarePoint.rbPoint.apply {
                                        x += dx
                                        y += dy
                                    }
                                    currentSquarePoint.rtPoint.x += dx
                                    currentSquarePoint.lbPoint.y += dy
                                }
                                DragType.LBPoint -> {
                                    currentSquarePoint.lbPoint.apply {
                                        x += dx
                                        y += dy
                                    }
                                    currentSquarePoint.ltPoint.x += dx
                                    currentSquarePoint.rbPoint.y += dy
                                }
                            }
                            setPathBySquarePoint(currentSquarePoint)
                            changeRectF.set(currentSquarePoint.toRectF())
                            invalidate()
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            config.ratio = changeRectF.width() / changeRectF.height()
                            calStandardRectF()
                            setPathByRectF(standardRectF)
                            changeRectF.setEmpty()
                            lastTouchPointF.set(-1f, -1f)
                            dragType = DragType.Image
                            invalidate()
                        }
                    }
                    return true
                } else
                    return super.onTouch(view, event)


            }
        }
    }

    // protected method
    protected fun calStandardRectF() {
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

    protected fun setPathByRectF(rectF: RectF) {
        val squarePoint = SquarePoint(
                PointF(rectF.left, rectF.top),
                PointF(rectF.right, rectF.top),
                PointF(rectF.right, rectF.bottom),
                PointF(rectF.left, rectF.bottom))
        setPathBySquarePoint(squarePoint)
    }

    protected fun setPathBySquarePoint(squarePoint: SquarePoint) {
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

    // private method
    private fun initAttrs(attributeSet: AttributeSet?) {
        attributeSet?.let {
            val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RectFView)
            try {
                (0..typedArray.indexCount).map { i ->
                    typedArray.getIndex(i)
                }.forEach { index ->
                    when (index) {
                        R.styleable.RectFView_trimBorderColor -> config.borderColor = typedArray.getColor(index, getColor(R.color.black))
                        R.styleable.RectFView_trimBgColor -> config.backgroundColor = typedArray.getColor(index, getColor(R.color.trans_black))
                        R.styleable.RectFView_showTrimBg -> config.showBackground = typedArray.getBoolean(index, false)
                        R.styleable.RectFView_minPadding -> config.minPadding = typedArray.getDimensionPixelSize(index, resources.getDimensionPixelSize(R.dimen.min_padding_default)).toFloat()
                        R.styleable.RectFView_trimBorderWidth -> config.borderWidth = typedArray.getDimensionPixelSize(index, resources.getDimensionPixelSize(R.dimen.border_width_default)).toFloat()
                        R.styleable.RectFView_ratio -> config.ratio = typedArray.getFloat(index, 1f)
                        R.styleable.RectFView_dragMode -> config.dragMode = when (typedArray.getInt(index, 0)) {
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

    private fun checkDragType(event: MotionEvent, rectF: RectF): DragType {
        val standardSquarePointF = SquarePoint.transForm(rectF)
        val halfWidth = dp2px(50f) / 2
        // left top area
        val ltRectF = RectF(standardSquarePointF.ltPoint.x - halfWidth / 2,
                standardSquarePointF.ltPoint.y - halfWidth / 2,
                standardSquarePointF.ltPoint.x + halfWidth / 2,
                standardSquarePointF.ltPoint.y + halfWidth / 2)
        // right top area
        val rtRectF = RectF(standardSquarePointF.rtPoint.x - halfWidth / 2,
                standardSquarePointF.rtPoint.y - halfWidth / 2,
                standardSquarePointF.rtPoint.x + halfWidth / 2,
                standardSquarePointF.rtPoint.y + halfWidth / 2)

        // right bottom area
        val rbRectF = RectF(standardSquarePointF.rbPoint.x - halfWidth / 2,
                standardSquarePointF.rbPoint.y - halfWidth / 2,
                standardSquarePointF.rbPoint.x + halfWidth / 2,
                standardSquarePointF.rbPoint.y + halfWidth / 2)
        // left bottom area
        val lbRectF = RectF(standardSquarePointF.lbPoint.x - halfWidth / 2,
                standardSquarePointF.lbPoint.y - halfWidth / 2,
                standardSquarePointF.lbPoint.x + halfWidth / 2,
                standardSquarePointF.lbPoint.y + halfWidth / 2)

        return when {
            ltRectF.contains(event.x, event.y) -> DragType.LTPoint
            rtRectF.contains(event.x, event.y) -> DragType.RTPoint
            rbRectF.contains(event.x, event.y) -> DragType.RBPoint
            lbRectF.contains(event.x, event.y) -> DragType.LBPoint
            else -> DragType.Image
        }
    }

    // inner class
    protected data class SquarePoint(
            val ltPoint: PointF = PointF(),
            val rtPoint: PointF = PointF(),
            val rbPoint: PointF = PointF(),
            val lbPoint: PointF = PointF()
    ) {

        fun toRectF() =
                RectF(ltPoint.x, ltPoint.y, rbPoint.x, rbPoint.y)

        companion object {
            fun transForm(rectF: RectF): SquarePoint =
                    SquarePoint(
                            PointF(rectF.left, rectF.top),
                            PointF(rectF.right, rectF.top),
                            PointF(rectF.right, rectF.bottom),
                            PointF(rectF.left, rectF.bottom))
        }
    }

    private sealed class DragType {
        object Image : DragType()
        object LTPoint : DragType()
        object RTPoint : DragType()
        object RBPoint : DragType()
        object LBPoint : DragType()
    }


}