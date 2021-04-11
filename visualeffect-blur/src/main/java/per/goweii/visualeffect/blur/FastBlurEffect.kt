package per.goweii.visualeffect.blur

import android.graphics.Bitmap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FastBlurEffect(radius: Float) : BlurEffect(radius) {
    private var pixels: IntArray? = null
    private var reds: IntArray? = null
    private var greens: IntArray? = null
    private var blues: IntArray? = null
    private var vmins: IntArray? = null
    private var divs: IntArray? = null

    override fun recycle() {
        super.recycle()
        pixels = null
        reds = null
        greens = null
        blues = null
        vmins = null
        divs = null
    }

    private fun preparePixels(bitmap: Bitmap) {
        val size = bitmap.width * bitmap.height
        if (pixels?.size != size) {
            pixels = IntArray(size)
        }
    }

    private fun prepareRGBs(bitmap: Bitmap) {
        val size = bitmap.width * bitmap.height
        if (reds?.size != size) {
            reds = IntArray(size)
        }
        if (greens?.size != size) {
            greens = IntArray(size)
        }
        if (blues?.size != size) {
            blues = IntArray(size)
        }
    }

    private fun prepareVmins(size: Int) {
        if (vmins?.size != size) {
            vmins = IntArray(size)
        }
    }

    private fun prepareDivs(size: Int) {
        if (divs?.size != size) {
            divs = IntArray(size)
        }
    }

    @Suppress("JoinDeclarationAndAssignment")
    override fun doEffect(input: Bitmap, output: Bitmap) {
        check(input.width == output.width && input.height == output.height)
        val radius = max(0, radius.toInt())
        if (radius == 0 && input === output) return
        val w = input.width
        val h = input.height
        preparePixels(input)
        val pix = pixels!!
        input.getPixels(pix, 0, w, 0, 0, w, h)
        if (radius == 0) {
            output.setPixels(pix, 0, w, 0, 0, w, h)
            return
        }
        prepareRGBs(input)
        val r = reds!!
        val g = greens!!
        val b = blues!!
        val wm = w - 1
        val hm = h - 1
        val div = radius + radius + 1
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        prepareVmins(max(w, h))
        val vmin = vmins!!
        var divsum = div + 1 shr 1
        divsum *= divsum
        prepareDivs(256 * divsum)
        val dv = divs!!
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + min(wm, max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                pix[yi] =
                    -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) {
                    vmin[y] = min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        output.setPixels(pix, 0, w, 0, 0, w, h)
    }
}