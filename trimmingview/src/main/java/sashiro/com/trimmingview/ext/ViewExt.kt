package sashiro.com.trimmingview.ext

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