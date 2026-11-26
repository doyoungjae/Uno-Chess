
package com.example.unochessprototype.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.ui.theme.UnoChessColors

enum class SettingsCategory(
    val title: String,
    val description: String,
    val emoji: String
) {
    PROFILE("프로필", "닉네임과 사용자 아이콘 관리", "👤"),
    FRIENDS("친구", "친구 목록과 친구 관리", "👥"),
    SOUND("사운드", "배경 음악과 효과음 설정", "🔊"),
    STATISTICS("통계", "나의 게임 기록과 승률 확인", "📊"),
    RATE_APP("앱 평가하기", "UNO Chess 앱 평가하기", "⭐")
}

@Composable
fun SettingsMainScreen(
    onCategoryClick: (SettingsCategory) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "설정",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = UnoChessColors.Text
                )

                Text(
                    text = "게임 환경과 계정을 관리하세요",
                    fontSize = 12.sp,
                    color = UnoChessColors.Muted
                )
            }
        }

        items(SettingsCategory.entries.toList()) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCategoryClick(category)
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = UnoChessColors.Surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.emoji,
                        fontSize = 27.sp
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            text = category.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = UnoChessColors.Text
                        )

                        Text(
                            text = category.description,
                            fontSize = 11.sp,
                            color = UnoChessColors.Muted
                        )
                    }

                    Text(
                        text = "›",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = UnoChessColors.Lime
                    )
                }
            }
        }
    }
}
