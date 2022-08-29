package com.bluelinelabs.conductor.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.Router.PopRootControllerMode
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.demo.controllers.HomeController
import com.bluelinelabs.conductor.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ToolbarProvider {
  private lateinit var binding: ActivityMainBinding
  private lateinit var router: Router

  override val toolbar: Toolbar
    get() = binding.toolbar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    router = Conductor.attachRouter(this, binding.controllerContainer, savedInstanceState)
      .setPopRootControllerMode(PopRootControllerMode.NEVER)
      .setOnBackPressedDispatcherEnabled(UseOnBackPressedDispatcher)

    if (!router.hasRootController()) {
      router.setRoot(RouterTransaction.with(HomeController()))
    }
  }

  override fun onBackPressed() {
    // This method shouldn't be overridden at all if we're using the OnBackPressedDispatcher
    if (UseOnBackPressedDispatcher) {
      super.onBackPressed()
      return
    }

    if (!router.handleBack()) {
      super.onBackPressed()
    }
  }
}

private const val UseOnBackPressedDispatcher = true
