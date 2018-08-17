package sashiro.com.trimmingview.model

sealed class DragMode {
    object Default : DragMode()
    object OverDrag : DragMode()
    object Disabled : DragMode()
}
