package com.bluelinelabs.conductor.changehandler

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler

/**
 * An [AnimatorChangeHandler] that will cross fade two views
 */
class FadeChangeHandler : AnimatorChangeHandler {
  constructor() : super()
  constructor(removesFromViewOnPush: Boolean) : super(removesFromViewOnPush)
  constructor(duration: Long) : super(duration)
  constructor(duration: Long, removesFromViewOnPush: Boolean) : super(duration, removesFromViewOnPush)

  override fun getAnimator(
    container: ViewGroup,
    from: View?,
    to: View?,
    isPush: Boolean,
    toAddedToContainer: Boolean,
  ): Animator {
    val animator = AnimatorSet()
    if (to != null) {
      val start = if (toAddedToContainer) 0F else to.alpha
      animator.play(ObjectAnimator.ofFloat(to, View.ALPHA, start, 1f))
    }
    if (from != null && (!isPush || removesFromViewOnPush)) {
      animator.play(ObjectAnimator.ofFloat(from, View.ALPHA, 0f))
    }
    return animator
  }

  override fun resetFromView(from: View) {
    from.alpha = 1f
  }

  override fun copy(): ControllerChangeHandler = FadeChangeHandler(animationDuration, removesFromViewOnPush)
}
