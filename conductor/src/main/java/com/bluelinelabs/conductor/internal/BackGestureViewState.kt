package com.bluelinelabs.conductor.internal

import android.view.View
import com.bluelinelabs.conductor.Controller

class BackGestureViewState(
  val fromView: View,
  val toViews: List<BackGestureControllerView>,
)

class BackGestureControllerView(
  val controller: Controller,
  val view: View,
  val inflatedForGesture: Boolean,
)
