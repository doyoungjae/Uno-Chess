package com.example.unochessprototype.ui.fragment

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.unochessprototype.ui.theme.UnoChessTheme

internal fun Fragment.composeView(content: @Composable () -> Unit): View =
    ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            UnoChessTheme(content = content)
        }
    }
