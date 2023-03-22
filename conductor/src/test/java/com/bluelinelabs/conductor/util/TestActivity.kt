package com.bluelinelabs.conductor.util

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router

class TestActivity : FragmentActivity() {

  lateinit var router: Router

  var changingConfigurations = false
  var destroying = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    router = Conductor.attachRouter(
      this,
      findViewById(android.R.id.content),
      savedInstanceState
    )
  }

  override fun isChangingConfigurations(): Boolean {
    return changingConfigurations
  }

  override fun isDestroyed(): Boolean {
    return destroying || super.isDestroyed()
  }
}