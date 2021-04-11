package per.goweii.android.visualeffect

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

@SuppressLint("ClickableViewAccessibility")
class DragGestureHelper private constructor(
    private val view: View
) : GestureDetector.SimpleOnGestureListener() {
    companion object {
        fun attach(view: View): DragGestureHelper {
            return DragGestureHelper(view)
        }
    }

    private val gestureDetector = GestureDetector(view.context, this)

    private var downX = 0F
    private var downY = 0F

    var onDoubleClick: (() -> Unit)? = null

    init {
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        downX = view.translationX
        downY = view.translationY
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        return onDoubleClick?.let {
            it.invoke()
            true
        } ?: false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val x = (e2.rawX - e1.rawX).toInt()
        val y = (e2.rawY - e1.rawY).toInt()
        view.apply {
            translationX = downX + x
            translationY = downY + y
        }
        return true
    }
}