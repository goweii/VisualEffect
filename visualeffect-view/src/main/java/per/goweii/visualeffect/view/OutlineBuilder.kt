package per.goweii.visualeffect.view

import android.graphics.Path
import android.view.View
import java.lang.ref.WeakReference

abstract class OutlineBuilder {
    private var outlineHelperRef: WeakReference<OutlineHelper>? = null

    fun attachToVisualEffectOutlineHelper(outlineHelper: OutlineHelper) {
        if (outlineHelperRef == null || outlineHelperRef!!.get() !== outlineHelper) {
            outlineHelperRef = WeakReference(outlineHelper)
        }
    }

    fun detachFromVisualEffectOutlineHelper() {
        if (outlineHelperRef != null) {
            outlineHelperRef!!.clear()
            outlineHelperRef = null
        }
    }

    fun invalidateOutline() {
        outlineHelperRef?.get()?.invalidateOutline()
    }

    open fun getSuggestedMinimumWidth(): Int = 0

    open fun getSuggestedMinimumHeight(): Int = 0

    abstract fun buildOutline(view: View, outline: Path)
}