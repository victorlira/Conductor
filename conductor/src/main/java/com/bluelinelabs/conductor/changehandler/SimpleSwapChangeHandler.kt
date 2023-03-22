package com.bluelinelabs.conductor.changehandler

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler

/**
 * A [ControllerChangeHandler] that will instantly swap Views with no animations or transitions.
 */
class SimpleSwapChangeHandler @JvmOverloads constructor(
  removesFromViewOnPush: Boolean = true,
) : ControllerChangeHandler(), View.OnAttachStateChangeListener {

  private var _removesFromViewOnPush = removesFromViewOnPush
  override val removesFromViewOnPush: Boolean
    get() = _removesFromViewOnPush

  override val isReusable = true

  private var canceled = false
  private var container: ViewGroup? = null
  private var changeListener: ControllerChangeCompletedListener? = null

  override fun saveToBundle(bundle: Bundle) {
    super.saveToBundle(bundle)
    bundle.putBoolean(KEY_REMOVES_FROM_ON_PUSH, removesFromViewOnPush)
  }

  override fun restoreFromBundle(bundle: Bundle) {
    super.restoreFromBundle(bundle)
    _removesFromViewOnPush = bundle.getBoolean(KEY_REMOVES_FROM_ON_PUSH)
  }

  override fun onAbortPush(newHandler: ControllerChangeHandler, newTop: Controller?) {
    super.onAbortPush(newHandler, newTop)
    canceled = true
  }

  override fun completeImmediately() {
    changeListener?.onChangeCompleted()
    changeListener = null

    container?.removeOnAttachStateChangeListener(this)
    container = null
  }

  override fun performChange(
    container: ViewGroup,
    from: View?,
    to: View?,
    isPush: Boolean,
    changeListener: ControllerChangeCompletedListener,
  ) {
    if (canceled) return

    if (from != null && (!isPush || removesFromViewOnPush)) {
      container.removeView(from)
    }
    if (to != null && to.parent == null) {
      container.addView(to)
    }

    if (container.windowToken != null) {
      changeListener.onChangeCompleted()
    } else {
      this.changeListener = changeListener
      this.container = container
      container.addOnAttachStateChangeListener(this)
    }
  }

  override fun onViewAttachedToWindow(v: View) {
    v.removeOnAttachStateChangeListener(this)

    changeListener?.onChangeCompleted()
    changeListener = null
    container?.removeOnAttachStateChangeListener(this)
    container = null
  }

  override fun onViewDetachedFromWindow(v: View) = Unit

  override fun copy(): ControllerChangeHandler = SimpleSwapChangeHandler(removesFromViewOnPush)
}

private const val KEY_REMOVES_FROM_ON_PUSH = "SimpleSwapChangeHandler.removesFromViewOnPush"
