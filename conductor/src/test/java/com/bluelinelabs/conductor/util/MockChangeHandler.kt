package com.bluelinelabs.conductor.util

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler

class MockChangeHandler @JvmOverloads constructor(
  private var removesFromViewOnPush: Boolean = false,
  var tag: String? = null,
  private val listener: ChangeHandlerListener? = null,
) : ControllerChangeHandler() {
  @JvmField
  var from: View? = null
  @JvmField
  var to: View? = null
  private var delayHandler: DelayHandler? = null

  fun delayTransaction(): DelayHandler {
    return object : DelayHandler() {
      override fun onDelayEnded() {
        val changeData = changeData ?: throw IllegalStateException("Attempting to end transaction delay before ready.")
        performChange(changeData)
      }
    }.also { delayHandler = it }
  }

  override fun performChange(
    container: ViewGroup,
    from: View?,
    to: View?,
    isPush: Boolean,
    changeListener: ControllerChangeCompletedListener
  ) {
    if (delayHandler != null) {
      delayHandler!!.changeData = ChangeData(container, from, to, isPush, changeListener)
    } else {
      performChange(ChangeData(container, from, to, isPush, changeListener))
    }
  }

  override fun removesFromViewOnPush(): Boolean {
    return removesFromViewOnPush
  }

  override fun saveToBundle(bundle: Bundle) {
    super.saveToBundle(bundle)
    bundle.putBoolean(KEY_REMOVES_FROM_VIEW_ON_PUSH, removesFromViewOnPush)
    bundle.putString(KEY_TAG, tag)
  }

  override fun restoreFromBundle(bundle: Bundle) {
    super.restoreFromBundle(bundle)
    removesFromViewOnPush = bundle.getBoolean(KEY_REMOVES_FROM_VIEW_ON_PUSH)
    tag = bundle.getString(KEY_TAG)
  }

  override fun copy(): ControllerChangeHandler {
    return MockChangeHandler(removesFromViewOnPush, tag, listener)
  }

  override fun isReusable() = true

  private fun performChange(changeData: ChangeData) {
    from = changeData.from
    to = changeData.to

    listener?.willStartChange()

    if (changeData.isPush) {
      if (to != null) {
        changeData.container.addView(to)
        listener?.didAttachOrDetach()
      }
      if (removesFromViewOnPush && from != null) {
        changeData.container.removeView(from)
      }
    } else {
      changeData.container.removeView(from)
      listener?.didAttachOrDetach()
      if (to != null) {
        changeData.container.addView(to)
      }
    }

    changeData.changeListener.onChangeCompleted()
    listener?.didEndChange()
  }

  companion object {
    private const val KEY_REMOVES_FROM_VIEW_ON_PUSH = "MockChangeHandler.removesFromViewOnPush"
    private const val KEY_TAG = "MockChangeHandler.tag"
    fun defaultHandler(): MockChangeHandler {
      return MockChangeHandler(true, null, null)
    }

    fun noRemoveViewOnPushHandler(): MockChangeHandler {
      return MockChangeHandler(false, null, null)
    }

    fun noRemoveViewOnPushHandler(tag: String?): MockChangeHandler {
      return MockChangeHandler(false, tag, null)
    }

    fun listeningChangeHandler(listener: ChangeHandlerListener): MockChangeHandler {
      return MockChangeHandler(true, null, listener)
    }

    fun taggedHandler(tag: String?, removeViewOnPush: Boolean): MockChangeHandler {
      return MockChangeHandler(removeViewOnPush, tag, null)
    }
  }

  open class ChangeHandlerListener {
    open fun willStartChange() {}
    open fun didAttachOrDetach() {}
    open fun didEndChange() {}
  }

  abstract class DelayHandler {
    var changeData: ChangeData? = null
    abstract fun onDelayEnded()
  }

  data class ChangeData(
    val container: ViewGroup,
    val from: View?,
    val to: View?,
    val isPush: Boolean,
    val changeListener: ControllerChangeCompletedListener,
  )
}