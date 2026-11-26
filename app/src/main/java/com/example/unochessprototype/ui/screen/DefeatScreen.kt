
package com.example.unochessprototype.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.unochessprototype.R

@Composable
fun DefeatScreen(onReplay: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.defeat_screen),
            contentDescription = "Defeat screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier
                .offset(
                    x = maxWidth * 0.23f,
                    y = maxHeight * 0.678f
                )
                .size(
                    width = maxWidth * 0.505f,
                    height = maxHeight * 0.067f
                )
                .clickable {
                    onReplay()
                }
        )
    }
}
