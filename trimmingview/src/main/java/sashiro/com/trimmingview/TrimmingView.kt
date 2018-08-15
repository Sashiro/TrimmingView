package sashiro.com.trimmingview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import sashiro.com.trimmingview.ext.*
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
    var config: TrimmingViewConfig by Delegates.observable(TrimmingViewConfig.Builder(context).build()) { _, _, _ ->
        needCalTriRectF = true
        needCalImg = true
        applyColor()
        requestLayout()
    }

    override fun onGlobalLayout() {
        if (needCalTriRectF) {
            if (centerPointF.isEmpty)
                setCenter(widthF / 2, heightF / 2)

            // calculate trimPath
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
            // trimPath
            trimBorderPath
            trimBorderPath.apply {
                reset()
                moveTo(left, top)
                lineTo(right, top)
                lineTo(right, bottom)
                lineTo(left, bottom)
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
            // setStandardRectF
            standardRectF.apply {
                this.left = left
                this.right = right
                this.top = top
                this.bottom = bottom
            }

            needCalTriRectF = false
        }


        // init Img
        super.onGlobalLayout()
    }

    init {
//        config = TrimmingViewConfig.Builder(context).build()
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
                config.borderColor = typedArray.getColor(R.styleable.TrimmingView_trimBorderColor, getColor(R.color.black))
                config.backgroundColor = typedArray.getColor(R.styleable.TrimmingView_trimBgColor, getColor(R.color.trans_black))
                config.showBackground = typedArray.getBoolean(R.styleable.TrimmingView_showTrimBg, false)
                config.minPadding = typedArray.getDimensionPixelSize(R.styleable.TrimmingView_minPadding, resources.getDimensionPixelSize(R.dimen.min_padding_default)).toFloat()
                config.borderWidth = typedArray.getDimensionPixelSize(R.styleable.TrimmingView_trimBorderWidth, resources.getDimensionPixelSize(R.dimen.border_width_default)).toFloat()
                config.ratio = typedArray.getFloat(R.styleable.TrimmingView_ratio, 1f)
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

    // public method
//    fun setConfig(config: TrimmingViewConfig) {
//        this.config.set(config)
//        needCalTriRectF = true
//        needCalImg = true
//        applyColor()
//        requestLayout()
//    }

    fun turnClockwise() =
            rotateImg(getCurrentAngle() + 90f)


    fun turnAnticlockwise() =
            rotateImg(getCurrentAngle() - 90f)

    override fun rotateImg(angle: Float): Float {
        // rotate trimPath
        rectFHasRotated = angle % 180 != 0f
        needCalTriRectF = true
        needCalImg = true
        return super.rotateImg(angle)
    }
}