package com.steamclock.debugmenu_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment

/**
 * debugmenu
 * Created by jake on 2021-12-02, 1:31 p.m.
 */
class DebugMenuDialogFragment(private val content: @Composable (DebugMenuDialogFragment) -> Unit) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                content(this@DebugMenuDialogFragment)
            }
        }
    }
}