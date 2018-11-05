package sashiro.com.trimmingview.model

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable

data class TrimmingResult(
        val trimmingRect: Rect,
        val angle: Float,
        val maxScale: Float
) : Parcelable {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(trimmingRect, flags)
        dest.writeFloat(angle)
        dest.writeFloat(maxScale)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<TrimmingResult> {
            override fun createFromParcel(src: Parcel): TrimmingResult =
                    TrimmingResult(
                            src.readParcelable(javaClass.classLoader),
                            src.readFloat(),
                            src.readFloat())

            override fun newArray(size: Int): Array<TrimmingResult> = arrayOf()
        }
    }
}