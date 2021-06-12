package per.goweii.visualeffect.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import per.goweii.visualeffect.core.VisualEffect

class BackdropVisualEffectFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
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

    init {
        setWillNotDraw(false)
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