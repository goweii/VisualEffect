package per.goweii.visualeffect.core

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import java.lang.Exception

abstract class ParcelableVisualEffect: VisualEffect, Parcelable {
    open fun readFromParcel(parcel: Parcel) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(javaClass.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableVisualEffect> {
        override fun createFromParcel(parcel: Parcel): ParcelableVisualEffect? {
            val className = parcel.readString()
            if (className.isNullOrEmpty()) return null
            val visualEffect = createVisualEffectByClassName(className) ?: return null
            visualEffect.readFromParcel(parcel)
            return visualEffect
        }

        override fun newArray(size: Int): Array<ParcelableVisualEffect?> {
            return arrayOfNulls(size)
        }

        private fun createVisualEffectByClassName(className: String?): ParcelableVisualEffect? {
            className ?: return null
            var cls: Class<*>? = null
            try {
                cls = Class.forName(className)
            } catch (e: Exception) {
            }
            cls ?: return null
            try {
                val c = cls.getConstructor()
                return c.newInstance() as ParcelableVisualEffect
            } catch (e: Exception) {
            }
            try {
                val c = cls.getConstructor(Context::class.java)
                return c.newInstance() as ParcelableVisualEffect
            } catch (e: Exception) {
            }
            return null
        }
    }

}