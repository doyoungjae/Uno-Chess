package com.example.unochessprototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.unochessprototype.ui.UnoChessApp
import com.example.unochessprototype.ui.theme.UnoChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UnoChessTheme {
                UnoChessApp()
            }
        }
    }
}
