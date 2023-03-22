package com.bluelinelabs.conductor.changehandler

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler

/**
 * An [AnimatorChangeHandler] that will slide the views left or right, depending on if it's a push or pop.
 */
class HorizontalChangeHandler : AnimatorChangeHandler {
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
    val animatorSet = AnimatorSet()
    if (isPush) {
      if (from != null) {
        animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, -from.width.toFloat()))
      }
      if (to != null) {
        animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, to.width.toFloat(), 0f))
      }
    } else {
      if (from != null) {
        animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, from.width.toFloat()))
      }
      if (to != null) {
        // Allow this to have a nice transition when coming off an aborted push animation
        val fromLeft = from?.translationX ?: 0F
        animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, fromLeft - to.width, 0f))
      }
    }
    return animatorSet
  }

  override fun resetFromView(from: View) {
    from.translationX = 0f
  }

  override fun copy(): ControllerChangeHandler = HorizontalChangeHandler(animationDuration, removesFromViewOnPush)
}
