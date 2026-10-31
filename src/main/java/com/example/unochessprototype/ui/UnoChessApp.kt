package com.example.unochessprototype.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalPreferences
import com.example.unochessprototype.data.LocalProfile
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.game.GameResult

private val AppBackground = Color(0xFF08140D)
private val AppSurface = Color(0xFF102219)
private val AppSurface2 = Color(0xFF173126)
private val AppGreen = Color(0xFFA8E85A)
private val AppGreenDark = Color(0xFF6AAA35)
private val AppText = Color(0xFFF1F6EE)
private val AppMuted = Color(0xFFAAB8AE)
private val AppBorder = Color(0xFF2D4A38)

enum class AppScreen { HOME, PROFILE, SETTINGS, LOBBY, GAME }

@Composable
fun UnoChessApp() {
    val context = LocalContext.current
    val preferences = remember { LocalPreferences(context) }
    var profile by remember { mutableStateOf(preferences.loadProfile()) }
    var settings by remember { mutableStateOf(preferences.loadSettings()) }
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    var opponentName by remember { mutableStateOf("PLAYER 2") }

    val colors = darkColorScheme(
        primary = AppGreen,
        onPrimary = Color(0xFF0B160D),
        secondary = AppGreenDark,
        background = AppBackground,
        surface = AppSurface,
        surfaceVariant = AppSurface2,
        onBackground = AppText,
        onSurface = AppText,
        outline = AppBorder,
    )

    MaterialTheme(colorScheme = colors) {
        Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
            when (screen) {
                AppScreen.HOME -> HomeScreen(
                    profile = profile,
                    onProfile = { screen = AppScreen.PROFILE },
                    onPlay = { screen = AppScreen.LOBBY },
                    onSettings = { screen = AppScreen.SETTINGS },
                )

                AppScreen.PROFILE -> ProfileScreen(
                    profile = profile,
                    onSave = { updated ->
                        profile = updated
                        preferences.saveProfile(updated)
                        screen = AppScreen.HOME
                    },
                    onBack = { screen = AppScreen.HOME },
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    settings = settings,
                    onSave = { updated ->
                        settings = updated
                        preferences.saveSettings(updated)
                        screen = AppScreen.HOME
                    },
                    onBack = { screen = AppScreen.HOME },
                )

                AppScreen.LOBBY -> LocalLobbyScreen(
                    profile = profile,
                    opponentName = opponentName,
                    onOpponentNameChange = { opponentName = it.take(16) },
                    onStart = { screen = AppScreen.GAME },
                    onBack = { screen = AppScreen.HOME },
                )

                AppScreen.GAME -> UnoChessGameScreen(
                    profile = profile,
                    opponentName = opponentName.ifBlank { "PLAYER 2" },
                    settings = settings,
                    onExit = { screen = AppScreen.HOME },
                    onGameFinished = { result ->
                        val updated = when (result) {
                            is GameResult.Win -> profile.copy(
                                games = profile.games + 1,
                                wins = profile.wins + if (result.winner.name == "WHITE") 1 else 0,
                            )
                            is GameResult.Draw -> profile.copy(
                                games = profile.games + 1,
                                draws = profile.draws + 1,
                            )
                        }
                        profile = updated
                        preferences.saveProfile(updated)
                    },
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    profile: LocalProfile,
    onProfile: () -> Unit,
    onPlay: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.clickable { onProfile() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AppSurface2, CircleShape)
                        .border(1.dp, AppBorder, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(profile.avatar, fontSize = 26.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(profile.nickname, fontWeight = FontWeight.Bold)
                    Text(
                        "${profile.wins}승 / ${profile.games}게임",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = AppSurface2,
                tonalElevation = 0.dp,
            ) {
                Text(
                    "LOCAL MODE",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    color = AppGreen,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("♞ UNO CHESS", fontWeight = FontWeight.Black, fontSize = 25.sp, color = AppGreen)
            Spacer(Modifier.height(8.dp))
            Text(
                "카드가 선택한 열에서 체스 기물을 움직이세요",
                color = AppMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(22.dp))
            HomeBoardPreview()
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
            ) {
                Text("▶  플레이", fontWeight = FontWeight.Black, fontSize = 17.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            HomeBottomButton("♟", "프로필", onProfile)
            HomeBottomButton("▶", "플레이", onPlay)
            HomeBottomButton("⚙", "설정", onSettings)
        }
    }
}

@Composable
private fun HomeBottomButton(icon: String, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, fontSize = 22.sp, color = AppGreen)
        Text(label, style = MaterialTheme.typography.labelMedium, color = AppMuted)
    }
}

@Composable
private fun HomeBoardPreview() {
    Card(
        modifier = Modifier.fillMaxWidth(0.85f),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val top = listOf("♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜")
            val bottom = listOf("♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖")
            repeat(8) { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(8) { col ->
                        val piece = when (row) {
                            0 -> top[col]
                            1 -> "♟"
                            6 -> "♙"
                            7 -> bottom[col]
                            else -> ""
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(31.dp)
                                .background(if ((row + col) % 2 == 0) Color(0xFFBED39C) else Color(0xFF47664A)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(piece, fontSize = 19.sp, color = if (row < 2) Color.Black else Color.White)
                        }
                    }
                }
            }
        }
    }
}
