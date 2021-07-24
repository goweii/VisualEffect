package per.goweii.visualeffect.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import per.goweii.visualeffect.core.VisualEffect
import java.text.NumberFormat
import kotlin.math.max

open class BackdropVisualEffectView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val visualEffectHelper = BackdropVisualEffectHelper(this)
        .apply {
            onCallSuperDraw = { super.draw(it) }
            onCallSuperRestoreInstanceState = { super.onRestoreInstanceState(it) }
            onCallSuperSaveInstanceState = { super.onSaveInstanceState() }
        }

    var overlayColor: Int
        get() = visualEffectHelper.overlayColor
        set(value) {
            visualEffectHelper.overlayColor = value
        }
    var visualEffect: VisualEffect?
        get() = visualEffectHelper.visualEffect
        set(value) {
            visualEffectHelper.visualEffect = value
        }
    var simpleSize: Float
        get() = visualEffectHelper.simpleSize
        set(value) {
            visualEffectHelper.simpleSize = value
        }
    var isShowDebugInfo
        get() = visualEffectHelper.isShowDebugInfo
        set(value) {
            visualEffectHelper.isShowDebugInfo = value
        }

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) {
        visualEffectHelper.draw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        visualEffectHelper.onDraw(canvas)
    }

    @SuppressLint("MissingSuperCall")
    override fun onRestoreInstanceState(state: Parcelable?) {
        visualEffectHelper.onRestoreInstanceState(state)
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(): Parcelable {
        return visualEffectHelper.onSaveInstanceState()
    }
}