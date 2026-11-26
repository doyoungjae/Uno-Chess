package com.example.unochessprototype.ui.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.ui.theme.UnoChessColors

@Composable
fun ProfileSettingsTabScreen(
    profile: OnlineProfile,
    profileLoading: Boolean,
    profileError: String?,
    saveMessage: String?,
    settings: LocalSettings,
    onSaveOnlineIdentity: (String, String) -> Unit,
    onSaveSettings: (LocalSettings) -> Unit,
    onRetryProfile: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    var nickname by rememberSaveable { mutableStateOf(profile.nickname) }
    var avatar by rememberSaveable { mutableStateOf(profile.avatar) }
    var moveHints by rememberSaveable(settings.moveHints) { mutableStateOf(settings.moveHints) }
    var showCoordinates by rememberSaveable(settings.showCoordinates) { mutableStateOf(settings.showCoordinates) }

    LaunchedEffect(profile.uid, profile.nickname, profile.avatar) {
        if (profile.uid.isNotBlank()) {
            nickname = profile.nickname
            avatar = profile.avatar
        }
    }

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
                TextButton(onClick = onBack) {
                    Text("← 설정", color = UnoChessColors.Lime)
                }
                Text("프로필 · 설정", fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("온라인 프로필과 게임 환경 설정", color = UnoChessColors.Muted, fontSize = 12.sp)
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

                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(nickname.ifBlank { "PLAYER" }, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            Text("온라인 대전에서 다른 사용자에게 표시되는 프로필", color = UnoChessColors.Muted, fontSize = 10.sp)
                        }

                        if (profileLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        }
                    }

                    if (profileError != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(profileError, color = UnoChessColors.Warning, fontSize = 11.sp)
                                Button(onClick = onRetryProfile) { Text("Firebase 다시 연결") }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it.take(16) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("온라인 닉네임") },
                        supportingText = { Text("방 목록과 온라인 대전에서 이 이름을 사용합니다. 최대 16자") },
                        singleLine = true,
                        enabled = !profileLoading,
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
                                    .clickable(enabled = !profileLoading) { avatar = item },
                                contentAlignment = Alignment.Center,
                            ) { Text(item, fontSize = 22.sp) }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox("경기", profile.onlineGames.toString(), Modifier.weight(1f))
                        StatBox("승", profile.onlineWins.toString(), Modifier.weight(1f))
                        StatBox("패", profile.onlineLosses.toString(), Modifier.weight(1f))
                        StatBox("무", profile.onlineDraws.toString(), Modifier.weight(1f))
                    }
                    Text("온라인 승률 ${profile.winRate}% · 로컬 대전은 이 전적에 포함되지 않습니다.", color = UnoChessColors.Muted, fontSize = 10.sp)
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
                    onSaveOnlineIdentity(nickname, avatar)
                    onSaveSettings(
                        settings.copy(
                            moveHints = moveHints,
                            showCoordinates = showCoordinates,
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !profileLoading,
            ) {
                Text("프로필 · 설정 저장", fontWeight = FontWeight.Black)
            }
        }

        if (saveMessage != null) {
            item {
                Text(
                    saveMessage,
                    modifier = Modifier.fillMaxWidth(),
                    color = UnoChessColors.Lime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontWeight = FontWeight.Black)
            Text(label, color = UnoChessColors.Muted, fontSize = 9.sp)
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
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
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
