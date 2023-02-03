package com.bluelinelabs.conductor

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.UiThread
import com.bluelinelabs.conductor.internal.LifecycleHandler
import com.bluelinelabs.conductor.internal.ensureMainThread

object Conductor {

  /**
   * Conductor will create a [Router] that has been initialized for your Activity and containing ViewGroup.
   * If an existing [Router] is already associated with this Activity/ViewGroup pair, either in memory
   * or in the savedInstanceState, that router will be used and rebound instead of creating a new one with
   * an empty backstack.
   *
   * @param activity The Activity that will host the [Router] being attached.
   * @param container The ViewGroup in which the [Router]'s [Controller] views will be hosted
   * @param savedInstanceState The savedInstanceState passed into the hosting Activity's onCreate method. Used
   * for restoring the Router's state if possible.
   * @param allowExperimentalAndroidXBacking Use AndroidX backing if true and if the activity parameter is a
   * FragmentActivity.
   * @return A fully configured [Router] instance for use with this Activity/ViewGroup pair.
   */
  @UiThread
  @JvmStatic
  fun attachRouter(
    activity: Activity,
    container: ViewGroup,
    savedInstanceState: Bundle?,
    allowExperimentalAndroidXBacking: Boolean = true,
  ): Router {
    ensureMainThread()
    return LifecycleHandler.install(activity, allowAndroidXBacking = allowExperimentalAndroidXBacking)
      .getRouter(container, savedInstanceState)
      .also { it.rebindIfNeeded() }
  }
}
