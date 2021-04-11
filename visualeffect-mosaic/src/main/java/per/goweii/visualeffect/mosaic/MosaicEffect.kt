package per.goweii.visualeffect.mosaic

import android.graphics.Bitmap
import per.goweii.visualeffect.core.BaseVisualEffect
import kotlin.math.ceil
import kotlin.math.max

class MosaicEffect(var boxSize: Int) : BaseVisualEffect() {
    private var pixels: IntArray? = null

    override fun recycle() {
        super.recycle()
        pixels = null
    }

    private fun preparePixels(bitmap: Bitmap) {
        val size = bitmap.width * bitmap.height
        if (pixels?.size != size) {
            pixels = IntArray(size)
        }
    }

    override fun doEffect(input: Bitmap, output: Bitmap) {
        check(input.width == output.width && input.height == output.height)
        val boxSize = max(1, boxSize)
        if (boxSize == 1 && input === output) return
        val w = input.width
        val h = input.height
        preparePixels(input)
        val pix = pixels!!
        input.getPixels(pix, 0, w, 0, 0, w, h)
        if (boxSize == 1) {
            output.setPixels(pix, 0, w, 0, 0, w, h)
            return
        }
        val rowCount = ceil(w.toFloat() / boxSize).toInt()
        val columnCount = ceil(h.toFloat() / boxSize).toInt()
        for (r in 0 until rowCount) {
            for (c in 0 until columnCount) {
                val startX = c * boxSize + 1
                val startY = r * boxSize + 1
                dimBlock(pix, startX, startY, boxSize, w, h)
            }
        }
        output.setPixels(pix, 0, w, 0, 0, w, h)
    }

    private fun dimBlock(
        pixels: IntArray,
        startX: Int,
        startY: Int,
        blockSize: Int,
        maxX: Int,
        maxY: Int
    ) {
        var stopX = startX + blockSize - 1
        var stopY = startY + blockSize - 1
        if (stopX > maxX) {
            stopX = maxX
        }
        if (stopY > maxY) {
            stopY = maxY
        }
        //
        var sampleColorX = startX + blockSize / 2
        var sampleColorY = startY + blockSize / 2
        //
        if (sampleColorX > maxX) {
            sampleColorX = maxX
        }
        if (sampleColorY > maxY) {
            sampleColorY = maxY
        }
        val colorLinePosition = (sampleColorY - 1) * maxX
        val sampleColor = pixels[colorLinePosition + sampleColorX - 1]
        for (y in startY..stopY) {
            val p = (y - 1) * maxX
            for (x in startX..stopX) {
                pixels[p + x - 1] = sampleColor
            }
        }
    }

}