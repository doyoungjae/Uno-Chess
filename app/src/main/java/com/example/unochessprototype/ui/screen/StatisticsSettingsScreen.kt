
package com.example.unochessprototype.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.ui.theme.UnoChessColors

@Composable
fun StatisticsSettingsScreen(
    profile: OnlineProfile,
    onBack: () -> Unit
) {
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
            text = "통계",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = UnoChessColors.Text
        )

        Text(
            text = "나의 온라인 게임 기록",
            fontSize = 12.sp,
            color = UnoChessColors.Muted
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = UnoChessColors.Surface
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "게임 기록",
                    color = UnoChessColors.Text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                StatisticRow("전체 게임", profile.onlineGames.toString())
                StatisticRow("승리", profile.onlineWins.toString())
                StatisticRow("패배", profile.onlineLosses.toString())
                StatisticRow("무승부", profile.onlineDraws.toString())
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = UnoChessColors.Surface
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "승률",
                    color = UnoChessColors.Muted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${profile.winRate}%",
                    color = UnoChessColors.Lime,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "※ 온라인 게임 기록만 표시됩니다.",
            color = UnoChessColors.Muted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StatisticRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = UnoChessColors.Muted,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = UnoChessColors.Text,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
