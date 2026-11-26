package com.example.unochessprototype.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.ui.theme.UnoChessColors

/** 앱 시작 스플래시: R/G/B/Y 카드 부채 위에 KING 로고 + 타이틀. */
@Composable
fun SplashScreen() {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            // 스플래시가 떠 있는 동안 뒤 화면 터치를 막는다.
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.graphicsLayer {
                alpha = appear.value
                val scale = 0.85f + 0.15f * appear.value
                scaleX = scale
                scaleY = scale
            },
        ) {
            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                val cards = listOf(
                    Color(0xFFE53935) to -27f,
                    Color(0xFF1E88E5) to -9f,
                    Color(0xFF43A047) to 9f,
                    Color(0xFFFDD835) to 27f,
                )
                cards.forEachIndexed { index, (color, angle) ->
                    Box(
                        modifier = Modifier
                            .offset(x = ((index - 1.5f) * 16 * appear.value).dp, y = 8.dp)
                            .rotate(angle * appear.value)
                            .width(62.dp)
                            .height(92.dp)
                            .background(color, RoundedCornerShape(10.dp))
                            .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(10.dp)),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .background(UnoChessColors.Surface, RoundedCornerShape(33.dp))
                        .border(3.dp, UnoChessColors.Lime, RoundedCornerShape(33.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("♚︎", fontSize = 38.sp, color = UnoChessColors.Lime)
                }
            }
            Text("UNO CHESS", fontWeight = FontWeight.Black, fontSize = 34.sp, color = UnoChessColors.Lime)
            Text("카드로 열을 열고, 체스로 승부하세요", color = UnoChessColors.Muted, fontSize = 13.sp)
        }
    }
}
