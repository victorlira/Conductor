package com.bluelinelabs.conductor.changehandler

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler

/**
 * A base [ControllerChangeHandler] that facilitates using [android.animation.Animator]s to replace Controller Views
 */
abstract class AnimatorChangeHandler @JvmOverloads constructor(
  animationDuration: Long = DEFAULT_ANIMATION_DURATION,
  removesFromViewOnPush: Boolean = true,
) : ControllerChangeHandler() {

  var animationDuration: Long = animationDuration
    private set

  private var canceled = false
  private var needsImmediateCompletion = false
  private var completed = false
  private var animator: Animator? = null
  private var onAnimationReadyOrAbortedListener: OnAnimationReadyOrAbortedListener? = null

  private var _removesFromViewOnPush = removesFromViewOnPush
  override val removesFromViewOnPush: Boolean
    get() = _removesFromViewOnPush

  constructor(removesFromViewOnPush: Boolean = true) : this(
    animationDuration = DEFAULT_ANIMATION_DURATION,
    removesFromViewOnPush = removesFromViewOnPush,
  )

  override fun saveToBundle(bundle: Bundle) {
    super.saveToBundle(bundle)
    bundle.putLong(KEY_DURATION, animationDuration)
    bundle.putBoolean(KEY_REMOVES_FROM_ON_PUSH, removesFromViewOnPush)
  }

  override fun restoreFromBundle(bundle: Bundle) {
    super.restoreFromBundle(bundle)
    animationDuration = bundle.getLong(KEY_DURATION)
    _removesFromViewOnPush = bundle.getBoolean(KEY_REMOVES_FROM_ON_PUSH)
  }

  override fun onAbortPush(newHandler: ControllerChangeHandler, newTop: Controller?) {
    super.onAbortPush(newHandler, newTop)

    canceled = true
    if (animator != null) {
      animator!!.cancel()
    } else if (onAnimationReadyOrAbortedListener != null) {
      onAnimationReadyOrAbortedListener!!.onReadyOrAborted()
    }
  }

  override fun completeImmediately() {
    super.completeImmediately()

    needsImmediateCompletion = true
    if (animator != null) {
      animator!!.end()
    } else if (onAnimationReadyOrAbortedListener != null) {
      onAnimationReadyOrAbortedListener!!.onReadyOrAborted()
    }
  }

  /**
   * Should be overridden to return the Animator to use while replacing Views.
   *
   * @param container The container these Views are hosted in.
   * @param from The previous View in the container or `null` if there was no Controller before this transition
   * @param to The next View that should be put in the container or `null` if no Controller is being transitioned to
   * @param isPush True if this is a push transaction, false if it's a pop.
   * @param toAddedToContainer True if the "to" view was added to the container as a part of this ChangeHandler. False if it was already in the hierarchy.
   */
  protected abstract fun getAnimator(
    container: ViewGroup,
    from: View?,
    to: View?,
    isPush: Boolean,
    toAddedToContainer: Boolean,
  ): Animator

  /**
   * Will be called after the animation is complete to reset the View that was removed to its pre-animation state.
   */
  protected abstract fun resetFromView(from: View)

  override fun performChange(
    container: ViewGroup,
    from: View?,
    to: View?,
    isPush: Boolean,
    changeListener: ControllerChangeCompletedListener,
  ) {
    var readyToAnimate = true
    val addingToView = to != null && to.parent == null

    if (addingToView) {
      if (isPush || from == null) {
        container.addView(to)
      } else if (to!!.parent == null) {
        container.addView(to, container.indexOfChild(from))
      }
      if (to!!.width <= 0 && to.height <= 0) {
        readyToAnimate = false
        onAnimationReadyOrAbortedListener = OnAnimationReadyOrAbortedListener(container, from, to, isPush, true, changeListener)
        to.viewTreeObserver.addOnPreDrawListener(onAnimationReadyOrAbortedListener)
      }
    }

    if (readyToAnimate) {
      performAnimation(container, from, to, isPush, addingToView, changeListener)
    }
  }

  fun complete(changeListener: ControllerChangeCompletedListener, animatorListener: Animator.AnimatorListener?) {
    if (!completed) {
      completed = true
      changeListener.onChangeCompleted()
    }

    if (animator != null) {
      if (animatorListener != null) {
        animator!!.removeListener(animatorListener)
      }
      animator!!.cancel()
      animator = null
    }

    onAnimationReadyOrAbortedListener = null
  }

  fun performAnimation(
    container: ViewGroup,
    from: View?,
    to: View?,
    isPush: Boolean,
    toAddedToContainer: Boolean,
    changeListener: ControllerChangeCompletedListener,
  ) {
    if (canceled) {
      complete(changeListener, null)
      return
    }

    if (needsImmediateCompletion) {
      if (from != null && (!isPush || removesFromViewOnPush)) {
        container.removeView(from)
      }
      complete(changeListener, null)
      if (isPush && from != null) {
        resetFromView(from)
      }
      return
    }

    animator = getAnimator(container, from, to, isPush, toAddedToContainer)
    if (animationDuration > 0) {
      animator!!.duration = animationDuration
    }

    animator!!.addListener(
      object : AnimatorListenerAdapter() {
        override fun onAnimationCancel(animation: Animator) {
          from?.let { resetFromView(it) }
          if (to != null && to.parent === container) {
            container.removeView(to)
          }
          complete(changeListener, this)
        }

        override fun onAnimationEnd(animation: Animator) {
          if (!canceled && animator != null) {
            if (from != null && (!isPush || removesFromViewOnPush)) {
              container.removeView(from)
            }
            complete(changeListener, this)
            if (isPush && from != null) {
              resetFromView(from)
            }
          }
        }
      },
    )
    animator!!.start()
  }

  private inner class OnAnimationReadyOrAbortedListener constructor(
    val container: ViewGroup,
    val from: View?,
    val to: View?,
    val isPush: Boolean,
    val addingToView: Boolean,
    val changeListener: ControllerChangeCompletedListener,
  ) : ViewTreeObserver.OnPreDrawListener {

    private var hasRun = false
    override fun onPreDraw(): Boolean {
      onReadyOrAborted()
      return true
    }

    fun onReadyOrAborted() {
      if (!hasRun) {
        hasRun = true
        if (to != null) {
          val observer = to.viewTreeObserver
          if (observer.isAlive) {
            observer.removeOnPreDrawListener(this)
          }
        }
        performAnimation(container, from, to, isPush, addingToView, changeListener)
      }
    }
  }

  companion object {
    private const val KEY_DURATION = "AnimatorChangeHandler.duration"
    private const val KEY_REMOVES_FROM_ON_PUSH = "AnimatorChangeHandler.removesFromViewOnPush"
    const val DEFAULT_ANIMATION_DURATION: Long = -1
  }
}
