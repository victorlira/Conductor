package com.bluelinelabs.conductor.internal

import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray

internal class StringSparseArrayParceler(val stringSparseArray: SparseArray<String>) : Parcelable {

  override fun describeContents(): Int = 0

  override fun writeToParcel(out: Parcel, flags: Int) {
    val size = stringSparseArray.size()
    out.writeInt(size)
    for (i in 0 until size) {
      val key = stringSparseArray.keyAt(i)
      out.writeInt(key)
      out.writeString(stringSparseArray[key])
    }
  }

  companion object {

    @Suppress("unused")
    @JvmField
    val CREATOR: Parcelable.Creator<StringSparseArrayParceler> =
      object : Parcelable.Creator<StringSparseArrayParceler> {
        override fun createFromParcel(parcel: Parcel): StringSparseArrayParceler {
          val stringSparseArray = SparseArray<String>()
          val size = parcel.readInt()
          for (i in 0 until size) {
            stringSparseArray.put(parcel.readInt(), parcel.readString())
          }
          return StringSparseArrayParceler(stringSparseArray)
        }

        override fun newArray(size: Int): Array<StringSparseArrayParceler?> = arrayOfNulls(size)
      }
  }
}