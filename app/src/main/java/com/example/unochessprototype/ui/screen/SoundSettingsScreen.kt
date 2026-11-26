
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.ui.theme.UnoChessColors

@Composable
fun SoundSettingsScreen(
    settings: LocalSettings,
    onSettingsChange: (LocalSettings) -> Unit,
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
            text = "사운드",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = UnoChessColors.Text
        )

        Text(
            text = "음악과 효과음의 크기를 조절하세요",
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
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "배경 음악",
                            color = UnoChessColors.Text,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "게임 배경 음악 켜기/끄기",
                            color = UnoChessColors.Muted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settings.bgmEnabled,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(bgmEnabled = it))
                        }
                    )
                }

                Text(
                    "음악 볼륨 ${(settings.bgmVolume * 100).toInt()}%",
                    color = UnoChessColors.Text
                )
                Slider(
                    value = settings.bgmVolume,
                    onValueChange = {
                        onSettingsChange(settings.copy(bgmVolume = it))
                    },
                    valueRange = 0f..1f,
                    enabled = settings.bgmEnabled
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = UnoChessColors.Surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "효과음",
                            color = UnoChessColors.Text,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "이동 및 게임 효과음 켜기/끄기",
                            color = UnoChessColors.Muted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settings.effectsEnabled,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(effectsEnabled = it))
                        }
                    )
                }

                Text(
                    "효과음 볼륨 ${(settings.effectsVolume * 100).toInt()}%",
                    color = UnoChessColors.Text
                )
                Slider(
                    value = settings.effectsVolume,
                    onValueChange = {
                        onSettingsChange(settings.copy(effectsVolume = it))
                    },
                    valueRange = 0f..1f,
                    enabled = settings.effectsEnabled
                )
            }
        }
    }
}
