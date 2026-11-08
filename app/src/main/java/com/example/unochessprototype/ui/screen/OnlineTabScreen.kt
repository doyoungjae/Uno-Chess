package com.example.unochessprototype.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.online.model.OnlineRoom
import com.example.unochessprototype.ui.theme.UnoChessColors

@Composable
fun OnlineTabScreen(
    profile: OnlineProfile,
    connected: Boolean,
    rooms: List<OnlineRoom>,
    loadingRooms: Boolean,
    busy: Boolean,
    errorMessage: String?,
    onBackToHome: () -> Unit,
    onCreateRoom: (String) -> Unit,
    onJoinRoom: (String) -> Unit,
) {
    BackHandler(onBack = onBackToHome)
    var showCreateDialog by remember { mutableStateOf(false) }
    var roomName by remember(profile.nickname) { mutableStateOf("${profile.nickname}의 방") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("온라인 대전", fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("${profile.nickname} 님의 실시간 로비", color = UnoChessColors.Muted, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier.size(44.dp).background(UnoChessColors.SurfaceRaised, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (connected) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                        contentDescription = null,
                        tint = if (connected) UnoChessColors.Lime else UnoChessColors.Warning,
                    )
                }
            }
        }

        if (errorMessage != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
                    Text(
                        errorMessage,
                        modifier = Modifier.padding(12.dp),
                        color = UnoChessColors.Warning,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        item {
            Button(
                onClick = { showCreateDialog = true },
                enabled = connected && !busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Text("  방 생성하기", fontWeight = FontWeight.Black)
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Groups, contentDescription = null, tint = UnoChessColors.Lime)
                Text("입장 가능한 방 · 내 방", fontWeight = FontWeight.Black, fontSize = 17.sp)
            }
        }

        when {
            loadingRooms -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            }

            rooms.isEmpty() -> item {
                Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("입장 가능한 방이 없습니다.", fontWeight = FontWeight.Bold)
                        Text("새 방을 만들면 다른 사용자의 목록에 실시간으로 표시됩니다.", color = UnoChessColors.Muted, fontSize = 11.sp)
                    }
                }
            }

            else -> items(rooms, key = { it.roomId }) { room ->
                OnlineRoomCard(
                    room = room,
                    isMyRoom = room.isParticipant(profile.uid),
                    enabled = connected && !busy,
                    onJoin = { onJoinRoom(room.roomId) },
                )
            }
        }

        item {
            Text(
                "온라인 대전의 경기·승·패·무 전적만 프로필에 기록됩니다. 로컬 대전은 전적에 포함되지 않습니다.",
                color = UnoChessColors.Muted,
                fontSize = 10.sp,
            )
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!busy) showCreateDialog = false },
            title = { Text("온라인 방 생성") },
            text = {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it.take(24) },
                    label = { Text("방 이름") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = connected && !busy,
                    onClick = {
                        showCreateDialog = false
                        onCreateRoom(roomName)
                    },
                ) { Text("생성") }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { showCreateDialog = false }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun OnlineRoomCard(
    room: OnlineRoom,
    isMyRoom: Boolean,
    enabled: Boolean,
    onJoin: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(UnoChessColors.SurfaceRaised, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text(room.ownerAvatar, fontSize = 25.sp) }

            Column(modifier = Modifier.weight(1f)) {
                Text(room.roomName.ifBlank { "온라인 방" }, fontWeight = FontWeight.Black)
                Text("방장 ${room.ownerName}", color = UnoChessColors.Muted, fontSize = 11.sp)
                Text("${if (room.guestUid.isBlank()) 1 else 2} / 2 · ${room.status}", color = UnoChessColors.Lime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(enabled = enabled, onClick = onJoin) {
                Text(if (isMyRoom) "다시 입장" else "입장")
            }
        }
    }
}
