package com.example.unochessprototype.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalSettings

@Composable
fun SettingsScreen(
    settings: LocalSettings,
    onSave: (LocalSettings) -> Unit,
    onBack: () -> Unit,
) {
    var draft by remember { mutableStateOf(settings) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("환경 설정", fontSize = 22.sp, fontWeight = FontWeight.Black)
            OutlinedButton(onClick = onBack) { Text("뒤로") }
        }

        SettingSection("오디오") {
            SettingSwitch("배경 음악", draft.bgmEnabled) { draft = draft.copy(bgmEnabled = it) }
            Text("BGM 볼륨 ${(draft.bgmVolume * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFFAAB8AE))
            Slider(
                value = draft.bgmVolume,
                onValueChange = { draft = draft.copy(bgmVolume = it) },
                enabled = draft.bgmEnabled,
            )
            SettingSwitch("게임 효과음", draft.effectsEnabled) { draft = draft.copy(effectsEnabled = it) }
            Text("효과음 볼륨 ${(draft.effectsVolume * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFFAAB8AE))
            Slider(
                value = draft.effectsVolume,
                onValueChange = { draft = draft.copy(effectsVolume = it) },
                enabled = draft.effectsEnabled,
            )
            Text(
                "현재 버전은 오디오 파일이 없어 설정값만 로컬에 저장합니다.",
                fontSize = 12.sp,
                color = Color(0xFFAAB8AE),
            )
        }

        SettingSection("게임 화면") {
            SettingSwitch("이동 가능 칸 표시", draft.moveHints) { draft = draft.copy(moveHints = it) }
            SettingSwitch("체스판 좌표 표시", draft.showCoordinates) { draft = draft.copy(showCoordinates = it) }

            Text("체스판 테마", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { draft = draft.copy(boardTheme = 0) },
                    modifier = Modifier.weight(1f),
                ) { Text(if (draft.boardTheme == 0) "✓ 포레스트" else "포레스트") }
                OutlinedButton(
                    onClick = { draft = draft.copy(boardTheme = 1) },
                    modifier = Modifier.weight(1f),
                ) { Text(if (draft.boardTheme == 1) "✓ 클래식" else "클래식") }
            }
        }

        Spacer(Modifier.weight(1f))
        Button(onClick = { onSave(draft) }, modifier = Modifier.fillMaxWidth()) {
            Text("설정 저장")
        }
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102219)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, fontWeight = FontWeight.Black, color = Color(0xFFA8E85A))
            content()
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
