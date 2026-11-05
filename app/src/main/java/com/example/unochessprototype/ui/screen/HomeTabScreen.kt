package com.example.unochessprototype.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalProfile
import com.example.unochessprototype.ui.theme.UnoChessColors

@Composable
fun HomeTabScreen(
    profile: LocalProfile,
    onLocalGame: () -> Unit,
    onOnlineGame: () -> Unit,
    onProfile: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("UNO CHESS", fontWeight = FontWeight.Black, fontSize = 25.sp, color = UnoChessColors.Lime)
                    Text("카드로 열을 열고, 체스로 승부하세요", color = UnoChessColors.Muted, fontSize = 12.sp)
                }
                Surface(
                    modifier = Modifier.clickable { onProfile() },
                    shape = RoundedCornerShape(18.dp),
                    color = UnoChessColors.SurfaceRaised,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).background(UnoChessColors.SurfaceSoft, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(profile.avatar, fontSize = 21.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(profile.nickname, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(profile.userId, color = UnoChessColors.Muted, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        item { HeroBoardCard() }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip("게임", profile.games.toString(), Modifier.weight(1f))
                StatChip("승리", profile.wins.toString(), Modifier.weight(1f))
                val rate = if (profile.games == 0) 0 else (profile.wins * 100 / profile.games)
                StatChip("승률", "$rate%", Modifier.weight(1f))
            }
        }

        item {
            Text("게임 모드", fontWeight = FontWeight.Black, fontSize = 17.sp)
        }

        item {
            GameModeCard(
                title = "로컬 게임",
                subtitle = "한 기기에서 2명이 번갈아 플레이",
                enabled = true,
                onClick = onLocalGame,
            )
        }

        item {
            GameModeCard(
                title = "온라인 게임",
                subtitle = "Firebase 실시간 대전 · 마지막 개발 단계에서 연결",
                enabled = false,
                onClick = onOnlineGame,
            )
        }
    }
}

@Composable
private fun HeroBoardCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                color = UnoChessColors.Lime.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Rounded.SportsEsports, contentDescription = null, tint = UnoChessColors.Lime, modifier = Modifier.size(17.dp))
                    Text("LOCAL PLAY READY", color = UnoChessColors.Lime, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
            MiniBoard()
            Text(
                "1~8 카드는 A~H 열을 활성화하고, JOKER는 움직일 수 있는 열을 자유롭게 선택합니다.",
                color = UnoChessColors.Muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MiniBoard() {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .background(Color(0xFF26382D), RoundedCornerShape(12.dp))
            .padding(5.dp),
    ) {
        repeat(8) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(8) { col ->
                    val piece = when (row) {
                        0 -> listOf("♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜")[col]
                        1 -> "♟"
                        6 -> "♙"
                        7 -> listOf("♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖")[col]
                        else -> ""
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(29.dp)
                            .background(if ((row + col) % 2 == 0) Color(0xFFC7D9A7) else Color(0xFF41634A)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(piece, fontSize = 18.sp, color = if (row < 2) Color(0xFF17201A) else Color(0xFFF7F2E7))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(label, color = UnoChessColors.Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(
                    if (enabled) UnoChessColors.Lime.copy(alpha = 0.14f) else UnoChessColors.SurfaceRaised,
                    RoundedCornerShape(15.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                if (enabled) {
                    Icon(Icons.Rounded.SportsEsports, contentDescription = null, tint = UnoChessColors.Lime)
                } else {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = UnoChessColors.Muted)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.Black)
                Text(subtitle, color = UnoChessColors.Muted, fontSize = 11.sp)
            }
            Icon(
                Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = if (enabled) UnoChessColors.Lime else UnoChessColors.Muted,
            )
        }
    }
}
