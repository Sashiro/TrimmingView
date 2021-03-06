package sashiro.com.trimmingview.ext

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.widget.ImageView

val ImageView.widthF
    get() = width.toFloat()

val ImageView.heightF
    get() = height.toFloat()

val ImageView.drawableW
    get() = drawable.intrinsicWidth

val ImageView.drawableH
    get() = drawable.intrinsicHeight

val ImageView.drawableWF
    get() = drawableW.toFloat()

val ImageView.drawableHF
    get() = drawableH.toFloat()

fun Context.getCompColor(@ColorRes colorId: Int) =
        ContextCompat.getColor(this, colorId)

fun Context.px2dp(pxValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

fun Context.dp2px(dipValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dipValue * scale + 0.5f).toInt()
}

fun ImageView.getColor(@ColorRes colorId: Int) =
        context.getCompColor(colorId)

fun ImageView.px2dp(pxValue: Float): Int = context.px2dp(pxValue)

fun ImageView.dp2px(dipValue: Float): Int = context.dp2px(dipValue)