package per.goweii.visualeffect.view

import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import per.goweii.visualeffect.core.VisualEffect
import kotlin.math.max

open class BackdropVisualEffectFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val effectHelper: BackdropVisualEffectHelper by lazy {
        BackdropVisualEffectHelper(this)
    }
    private val outlineHelper: OutlineHelper by lazy {
        OutlineHelper(this)
    }

    var overlayColor: Int
        get() = effectHelper.overlayColor
        set(value) {
            effectHelper.overlayColor = value
        }
    var visualEffect: VisualEffect?
        get() = effectHelper.visualEffect
        set(value) {
            effectHelper.visualEffect = value
        }
    var simpleSize: Float
        get() = effectHelper.simpleSize
        set(value) {
            effectHelper.simpleSize = value
        }
    var isShowDebugInfo: Boolean
        get() = effectHelper.isShowDebugInfo
        set(value) {
            effectHelper.isShowDebugInfo = value
        }
    var outlineBuilder: OutlineBuilder?
        get() = outlineHelper.outlineBuilder
        set(value) {
            outlineHelper.outlineBuilder = value
        }
    val isRendering get() = effectHelper.isRendering


    init {
        super.setWillNotDraw(false)
    }

    override fun getSuggestedMinimumWidth(): Int {
        return max(
            super.getSuggestedMinimumWidth(),
            outlineHelper.suggestedMinimumWidth
        )
    }

    override fun getSuggestedMinimumHeight(): Int {
        return max(
            super.getSuggestedMinimumHeight(),
            outlineHelper.suggestedMinimumHeight
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        outlineHelper.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        outlineHelper.onSizeChanged(w, h, oldw, oldh)
    }

    override fun draw(canvas: Canvas) {
        effectHelper.checkRendering()
        outlineHelper.draw(canvas) {
            super.draw(it)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        effectHelper.onDraw(canvas)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        effectHelper.onRestoreInstanceState(state) {
            super.onRestoreInstanceState(it)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return effectHelper.onSaveInstanceState {
            super.onSaveInstanceState()
        }
    }
}