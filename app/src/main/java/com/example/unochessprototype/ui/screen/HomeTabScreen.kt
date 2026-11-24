package com.example.unochessprototype.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.Palette
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.ui.theme.boardPalette
import com.example.unochessprototype.ui.theme.rememberLocalImage

@Composable
fun HomeTabScreen(
    profile: OnlineProfile,
    profileConnected: Boolean,
    settings: LocalSettings,
    onCustomize: () -> Unit,
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
                            Text(
                                if (profileConnected) "ONLINE PROFILE" else "CONNECTING...",
                                color = if (profileConnected) UnoChessColors.Lime else UnoChessColors.Muted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        item { HeroBoardCard(settings) }

        item {
            GameModeCard(
                title = "보드 & 카드 커스텀",
                subtitle = "보드판 · 카드 뒷면 테마와 내 사진 설정",
                icon = Icons.Rounded.Palette,
                onClick = onCustomize,
            )
        }

        item {
            Text("온라인 전적", fontWeight = FontWeight.Black, fontSize = 17.sp)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip("경기", profile.onlineGames.toString(), Modifier.weight(1f))
                StatChip("승리", profile.onlineWins.toString(), Modifier.weight(1f))
                StatChip("승률", "${profile.winRate}%", Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip("패배", profile.onlineLosses.toString(), Modifier.weight(1f))
                StatChip("무승부", profile.onlineDraws.toString(), Modifier.weight(1f))
            }
        }

        item {
            Text(
                "로컬 대전 결과는 온라인 전적에 반영되지 않습니다.",
                color = UnoChessColors.Muted,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun HeroBoardCard(settings: LocalSettings) {
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
                    Text("UNO CHESS READY", color = UnoChessColors.Lime, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
            MiniBoard(settings)
        }
    }
}

@Composable
internal fun MiniBoard(settings: LocalSettings) {
    val palette = boardPalette(settings.boardTheme)
    val boardImage = rememberLocalImage(settings.boardImagePath)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .background(palette.border, RoundedCornerShape(12.dp))
            .padding(5.dp),
    ) {
    boardImage?.let { Image(it, contentDescription = null, modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop) }
    Column(modifier = Modifier.fillMaxWidth()) {
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
                            .background(
                                (if ((row + col) % 2 == 0) palette.light else palette.dark)
                                    .let { if (boardImage != null) it.copy(alpha = 0.45f) else it }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(piece, fontSize = 18.sp, color = if (row < 2) Color(0xFF17201A) else Color(0xFFF7F2E7))
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(label, color = UnoChessColors.Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(
                    UnoChessColors.Lime.copy(alpha = 0.14f),
                    RoundedCornerShape(15.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = UnoChessColors.Lime,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.Black)
                Text(subtitle, color = UnoChessColors.Muted, fontSize = 11.sp)
            }
            Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = UnoChessColors.Lime)
        }
    }
}
