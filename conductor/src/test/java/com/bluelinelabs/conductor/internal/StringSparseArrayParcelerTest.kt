package com.bluelinelabs.conductor.internal

import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StringSparseArrayParcelerTest {

  @Test
  fun emptyArray() {
    SparseArray<String>().parcelAndUnParcel().size() shouldBeExactly 0
  }

  @Test
  fun arrayWithContents() {
    val array = SparseArray<String>()
    array.put(1, "one")
    array.put(7, "seven")

    val unParceled = array.parcelAndUnParcel()

    unParceled.size() shouldBeExactly 2
    unParceled[1] shouldBe "one"
    unParceled[7] shouldBe "seven"
  }

  private fun SparseArray<String>.parcelAndUnParcel(): SparseArray<String> {
    val parceler = StringSparseArrayParceler(this)

    val parcel = Parcel.obtain()
    parceler.writeToParcel(parcel, 0)

    parcel.setDataPosition(0)
    @Suppress("UNCHECKED_CAST")
    val creator = StringSparseArrayParceler::class.java.getField("CREATOR").get(null)
      as Parcelable.Creator<StringSparseArrayParceler>
    val unParceled = creator.createFromParcel(parcel)
    return unParceled.stringSparseArray.also {
      check(it !== this)
    }
  }
}