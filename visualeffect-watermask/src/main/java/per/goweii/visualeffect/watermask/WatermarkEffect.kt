package per.goweii.visualeffect.watermask

import android.graphics.Bitmap
import per.goweii.visualeffect.core.BaseVisualEffect

class WatermarkEffect(
    var text: String,
    var textColor: Int,
    var textSize: Float
) : BaseVisualEffect() {
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