package per.goweii.visualeffect.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.getActivity(): Activity? {
    if (this is Activity) {
        return this
    }
    if (this is ContextWrapper) {
        val ctx = this.baseContext
        if (ctx !== this) {
            return ctx.getActivity()
        }
    }
    return null
}