package com.bluelinelabs.conductor.internal

import android.view.View
import com.bluelinelabs.conductor.Controller

class BackGestureViewState(
  val controller: Controller,
  val view: View,
  val inflatedForGesture: Boolean,
)
