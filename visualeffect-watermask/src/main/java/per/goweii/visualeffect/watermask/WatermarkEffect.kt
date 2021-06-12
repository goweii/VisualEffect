package per.goweii.visualeffect.watermask

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcel
import per.goweii.visualeffect.core.BaseVisualEffect

class WatermarkEffect(
    var text: String = "",
    var textColor: Int = Color.BLACK,
    var textSize: Float = 24F
) : BaseVisualEffect() {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(text)
        parcel.writeInt(textColor)
        parcel.writeFloat(textSize)
    }

    override fun readFromParcel(parcel: Parcel) {
        super.readFromParcel(parcel)
        text = parcel.readString() ?: ""
        textColor = parcel.readInt()
        textSize = parcel.readFloat()
    }

    override fun doEffect(input: Bitmap, output: Bitmap) {
        useCanvas(output, true) { canvas, paint ->
            if (input !== output) {
                canvas.drawBitmap(input, 0F, 0F, paint)
            }
            paint.textSize = textSize
            paint.color = textColor
            canvas.drawText(
                text,
                output.width - paint.measureText(text),
                output.height - paint.fontMetrics.bottom,
                paint
            )
        }
    }
}