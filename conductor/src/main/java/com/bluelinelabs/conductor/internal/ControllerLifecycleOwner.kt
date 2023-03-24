package com.bluelinelabs.conductor.internal

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Controller.LifecycleListener

class ControllerLifecycleOwner(lifecycleController: Controller) : LifecycleOwner {
  private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this) // --> State.INITIALIZED

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  init {
    lifecycleController.addLifecycleListener(
      object : LifecycleListener() {
        override fun postContextAvailable(controller: Controller, context: Context) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE) // --> State.CREATED;
        }

        override fun postCreateView(controller: Controller, view: View) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START) // --> State.STARTED;
        }

        override fun postAttach(controller: Controller, view: View) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) // --> State.RESUMED;
        }

        override fun preDetach(controller: Controller, view: View) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE) // --> State.STARTED;
        }

        override fun preDestroyView(controller: Controller, view: View) {
          lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP) // --> State.CREATED;
        }

        override fun preContextUnavailable(controller: Controller, context: Context) {
          // do nothing
        }

        override fun preDestroy(controller: Controller) {
          // Only act on Controllers that have had at least the onContextAvailable call made on them.
          if (lifecycleRegistry.currentState != Lifecycle.State.INITIALIZED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY) // --> State.DESTROYED;
          }
        }
      },
    )
  }
}
