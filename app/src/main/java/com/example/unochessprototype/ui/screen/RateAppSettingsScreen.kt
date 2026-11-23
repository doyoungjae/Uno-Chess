package com.example.unochessprototype.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.unochessprototype.ui.theme.UnoChessColors

@Composable
fun RateAppSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← 설정", color = UnoChessColors.Lime)
        }

        Text(
            text = "앱 평가하기",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = UnoChessColors.Text
        )

        Text(
            text = "UNO Chess를 사용해 주셔서 감사합니다!",
            fontSize = 12.sp,
            color = UnoChessColors.Muted
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = UnoChessColors.Surface
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "⭐",
                    fontSize = 36.sp
                )

                Text(
                    text = "게임이 마음에 드셨나요?",
                    color = UnoChessColors.Text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Text(
                    text = "여러분의 의견은 UNO Chess를 더 좋게 만드는 데 도움이 됩니다.",
                    color = UnoChessColors.Muted,
                    fontSize = 12.sp
                )

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                "UNO Chess 앱 피드백"
                            )
                        }
                        runCatching {
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UnoChessColors.Lime
                    )
                ) {
                    Text(
                        "의견 보내기",
                        color = UnoChessColors.Text
                    )
                }
            }
        }
    }
}

