package com.example.unochessprototype.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.online.model.OnlineRoom
import com.example.unochessprototype.online.model.RoomStatus
import com.example.unochessprototype.ui.theme.UnoChessColors

@Composable
fun OnlineLobbyScreen(
    room: OnlineRoom?,
    profile: OnlineProfile,
    busy: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onReadyChange: (Boolean) -> Unit,
    onStartGame: () -> Unit,
    onGameStarted: () -> Unit,
) {
    BackHandler { if (!busy) onBack() }

    LaunchedEffect(room?.status, busy) {
        if (!busy && (room?.status == RoomStatus.PLAYING || room?.status == RoomStatus.FINISHED)) {
            onGameStarted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        OutlinedButton(onClick = onBack, enabled = !busy) { Text("뒤로 · 방 나가기") }

        if (errorMessage != null && room == null) {
            Text(errorMessage, color = UnoChessColors.Warning)
        }

        Column {
            Text("온라인 대기실", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(room?.roomName ?: "방 정보를 불러오는 중...", color = UnoChessColors.Muted, fontSize = 12.sp)
        }

        if (room == null) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                if (errorMessage == null) {
                    CircularProgressIndicator()
                } else {
                    OutlinedButton(onClick = onRetry, enabled = !busy) { Text("다시 연결") }
                }
            }
            return@Column
        }

        if (errorMessage != null) {
            Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
                Text(errorMessage, modifier = Modifier.padding(12.dp), color = UnoChessColors.Warning, fontSize = 11.sp)
            }
        }

        OnlineLobbyPlayerCard(
            avatar = room.ownerAvatar,
            name = room.ownerName,
            role = "HOST",
            ready = room.ownerReady,
        )
        OnlineLobbyPlayerCard(
            avatar = room.guestAvatar,
            name = room.guestName.ifBlank { "상대방을 기다리는 중" },
            role = "GUEST",
            ready = room.guestUid.isNotBlank() && room.guestReady,
        )

        Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface)) {
            Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("온라인 경기 규칙", color = UnoChessColors.Lime, fontWeight = FontWeight.Black)
                LobbyRule("두 플레이어가 모두 READY여야 방장이 시작할 수 있습니다.")
                LobbyRule("시작 시 1~8 숫자를 각각 뽑아 큰 숫자가 WHITE/선공입니다.")
                LobbyRule("사용한 카드와 보드 상태는 Firebase를 통해 실시간 동기화됩니다.")
                LobbyRule("KING을 직접 포획하면 즉시 승리합니다.")
            }
        }

        Spacer(Modifier.weight(1f))

        val isOwner = profile.uid == room.ownerUid
        val myReady = when (profile.uid) {
            room.ownerUid -> room.ownerReady
            room.guestUid -> room.guestReady
            else -> false
        }
        val canReady = room.isParticipant(profile.uid) && room.status != RoomStatus.PLAYING && room.status != RoomStatus.FINISHED

        OutlinedButton(
            onClick = { onReadyChange(!myReady) },
            enabled = canReady && !busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
            Text(if (myReady) "  준비 취소" else "  준비 완료", fontWeight = FontWeight.Black)
        }

        if (isOwner) {
            Button(
                onClick = onStartGame,
                enabled = room.guestUid.isNotBlank() && room.ownerReady && room.guestReady && !busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Text("  온라인 경기 시작", fontWeight = FontWeight.Black)
            }
        } else {
            Text(
                if (room.guestUid.isBlank()) "방장이 상대를 기다리고 있습니다." else "방장이 경기를 시작할 때까지 기다려 주세요.",
                color = UnoChessColors.Muted,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun OnlineLobbyPlayerCard(
    avatar: String,
    name: String,
    role: String,
    ready: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(UnoChessColors.SurfaceRaised, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text(avatar, fontSize = 27.sp) }
            Column(modifier = Modifier.weight(1f).padding(start = 11.dp)) {
                Text(name, fontWeight = FontWeight.Black)
                Text(role, color = UnoChessColors.Muted, fontSize = 10.sp)
            }
            Text(
                if (ready) "READY" else "WAITING",
                color = if (ready) UnoChessColors.Lime else UnoChessColors.Warning,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun LobbyRule(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("•", color = UnoChessColors.Lime, modifier = Modifier.padding(end = 7.dp))
        Text(text, color = UnoChessColors.Muted, fontSize = 11.sp)
    }
}
