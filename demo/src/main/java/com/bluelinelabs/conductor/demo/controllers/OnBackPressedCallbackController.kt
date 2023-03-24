package com.bluelinelabs.conductor.demo.controllers

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.demo.ToolbarProvider

class OnBackPressedCallbackController : Controller() {

  private val onBackPressedCallback = object : OnBackPressedCallback(false) {
    override fun handleOnBackPressed() {
      Toast.makeText(activity!!, "Back handled at the Controller level", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onContextAvailable(context: Context) {
    super.onContextAvailable(context)

    onBackPressedDispatcher?.addCallback(lifecycleOwner, onBackPressedCallback)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return ComposeView(container.context).apply {
      setContent {
        OnBackPressedDemo()
      }
    }
  }

  override fun onAttach(view: View) {
    super.onAttach(view)

    (activity as? ToolbarProvider)?.toolbar?.apply {
      title = "OnBackPressed Demo"
      menu.clear()
    }
  }

  @Composable
  fun OnBackPressedDemo(modifier: Modifier = Modifier) {
    val radioOptions = BackOption.values()
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

    BackHandler(enabled = selectedOption == BackOption.Composable) {
      Toast.makeText(activity!!, "Back handled at the Composable level", Toast.LENGTH_SHORT).show()
    }

    MaterialTheme {
      Column(
        modifier = modifier
          .fillMaxSize()
          .padding(top = 16.dp),
      ) {
        LaunchedEffect(selectedOption) {
          onBackPressedCallback.isEnabled = selectedOption == BackOption.Controller
        }

        radioOptions.forEach { option ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .selectable(
                selected = (option == selectedOption),
                onClick = {
                  onOptionSelected(option)
                },
              )
              .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            RadioButton(
              selected = (option == selectedOption),
              onClick = { onOptionSelected(option) },
            )
            Text(
              text = option.title,
              modifier = Modifier.padding(start = 16.dp),
            )
          }
        }
      }
    }
  }
}

private enum class BackOption(val title: String) {
  Controller("Handle back in Controller"),
  Composable("Handle back in Composable"),
  None("Disable back handlers"),
}
