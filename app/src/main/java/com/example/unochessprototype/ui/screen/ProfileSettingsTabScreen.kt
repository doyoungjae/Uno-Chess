package com.example.unochessprototype.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalProfile
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.ui.theme.boardPalette

@Composable
fun ProfileSettingsTabScreen(
    profile: LocalProfile,
    settings: LocalSettings,
    onSave: (LocalProfile, LocalSettings) -> Unit,
) {
    var nickname by rememberSaveable(profile.nickname) { mutableStateOf(profile.nickname) }
    var avatar by rememberSaveable(profile.avatar) { mutableStateOf(profile.avatar) }
    var bgmEnabled by rememberSaveable(settings.bgmEnabled) { mutableStateOf(settings.bgmEnabled) }
    var effectsEnabled by rememberSaveable(settings.effectsEnabled) { mutableStateOf(settings.effectsEnabled) }
    var bgmVolume by rememberSaveable(settings.bgmVolume) { mutableFloatStateOf(settings.bgmVolume) }
    var effectsVolume by rememberSaveable(settings.effectsVolume) { mutableFloatStateOf(settings.effectsVolume) }
    var moveHints by rememberSaveable(settings.moveHints) { mutableStateOf(settings.moveHints) }
    var showCoordinates by rememberSaveable(settings.showCoordinates) { mutableStateOf(settings.showCoordinates) }
    var boardTheme by rememberSaveable(settings.boardTheme) { mutableIntStateOf(settings.boardTheme) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text("프로필 · 설정", fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("로컬 프로필과 게임 환경 설정", color = UnoChessColors.Muted, fontSize = 12.sp)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
                shape = RoundedCornerShape(22.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(58.dp).background(UnoChessColors.SurfaceRaised, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Text(avatar, fontSize = 32.sp) }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(nickname.ifBlank { "PLAYER 1" }, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            Text(profile.userId, color = UnoChessColors.Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("온라인 구현 시 계정 UID와 연결 가능한 로컬 식별자", color = UnoChessColors.Muted, fontSize = 9.sp)
                        }
                    }

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it.take(16) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("닉네임") },
                        singleLine = true,
                    )

                    Text("사용자 아이콘", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("♞", "♛", "♜", "♝", "♚", "♟", "★", "◆").forEach { item ->
                            val selected = item == avatar
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .background(
                                        if (selected) UnoChessColors.Lime.copy(alpha = 0.18f) else UnoChessColors.SurfaceRaised,
                                        RoundedCornerShape(12.dp),
                                    )
                                    .then(
                                        if (selected) Modifier.border(1.5.dp, UnoChessColors.Lime, RoundedCornerShape(12.dp))
                                        else Modifier
                                    )
                                    .clickable { avatar = item },
                                contentAlignment = Alignment.Center,
                            ) { Text(item, fontSize = 22.sp) }
                        }
                    }
                }
            }
        }

        item { SectionTitle("사운드") }
        item {
            SettingCard {
                ToggleRow("배경 음악", "BGM 재생 여부", bgmEnabled) { bgmEnabled = it }
                Slider(value = bgmVolume, onValueChange = { bgmVolume = it }, enabled = bgmEnabled)
                ToggleRow("효과음", "카드 선택·기물 이동·포획 효과음", effectsEnabled) { effectsEnabled = it }
                Slider(value = effectsVolume, onValueChange = { effectsVolume = it }, enabled = effectsEnabled)
                Text("※ 실제 음원 파일은 추후 팀에서 선택한 뒤 SoundPool/Media3에 연결할 수 있습니다.", color = UnoChessColors.Muted, fontSize = 9.sp)
            }
        }

        item { SectionTitle("체스 보드 테마") }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    ThemeChoice(index, selected = boardTheme == index, modifier = Modifier.weight(1f)) {
                        boardTheme = index
                    }
                }
            }
        }

        item { SectionTitle("게임 표시") }
        item {
            SettingCard {
                ToggleRow("이동 가능 칸", "선택 기물의 이동/포획 힌트 표시", moveHints) { moveHints = it }
                ToggleRow("보드 좌표", "a-h / 1-8 좌표 표시", showCoordinates) { showCoordinates = it }
            }
        }

        item {
            Button(
                onClick = {
                    val newProfile = profile.copy(
                        nickname = nickname.ifBlank { "PLAYER 1" },
                        avatar = avatar,
                    )
                    val newSettings = settings.copy(
                        bgmEnabled = bgmEnabled,
                        effectsEnabled = effectsEnabled,
                        bgmVolume = bgmVolume,
                        effectsVolume = effectsVolume,
                        moveHints = moveHints,
                        showCoordinates = showCoordinates,
                        boardTheme = boardTheme,
                    )
                    onSave(newProfile, newSettings)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("설정 저장", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Black, fontSize = 16.sp)
}

@Composable
private fun SettingCard(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface)) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, color = UnoChessColors.Muted, fontSize = 10.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThemeChoice(
    themeIndex: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = boardPalette(themeIndex)
    Card(
        modifier = modifier
            .then(if (selected) Modifier.border(2.dp, UnoChessColors.Lime, RoundedCornerShape(16.dp)) else Modifier)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                repeat(4) { r ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(4) { c ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(16.dp)
                                    .background(if ((r + c) % 2 == 0) palette.light else palette.dark)
                            )
                        }
                    }
                }
            }
            Text(palette.label, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Black else FontWeight.Medium)
        }
    }
}
