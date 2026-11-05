package com.example.unochessprototype.ui.screen

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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalProfile
import com.example.unochessprototype.ui.theme.UnoChessColors

@Composable
fun LocalGameTabScreen(
    profile: LocalProfile,
    opponentName: String,
    player2Ready: Boolean,
    onOpponentNameChange: (String) -> Unit,
    onReadyChange: (Boolean) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column {
            Text("로컬 게임", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Pass & Play · 같은 기기에서 2인 대전", color = UnoChessColors.Muted, fontSize = 12.sp)
        }

        LobbyPlayerCard(profile.avatar, profile.nickname, "PLAYER 1", true)
        LobbyPlayerCard("♜", opponentName.ifBlank { "PLAYER 2" }, "PLAYER 2", player2Ready)

        OutlinedTextField(
            value = opponentName,
            onValueChange = onOpponentNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("PLAYER 2 닉네임") },
            supportingText = { Text("최대 16자 · 로컬 게임에서만 사용") },
            singleLine = true,
        )

        Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface)) {
            Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("매치 규칙", color = UnoChessColors.Lime, fontWeight = FontWeight.Black)
                RuleLine("손패는 항상 5장")
                RuleLine("숫자 1~8 → A~H 열 활성화")
                RuleLine("JOKER → 이동 가능한 열 자유 선택")
                RuleLine("시작 전 1~8 추첨으로 WHITE/선공 결정")
                RuleLine("상대 KING 직접 포획 시 즉시 승리")
            }
        }

        Spacer(Modifier.weight(1f))

        if (!player2Ready) {
            Button(
                onClick = { onReadyChange(true) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                Text("  PLAYER 2 준비 완료", fontWeight = FontWeight.Black)
            }
        } else {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Text("  선공 결정으로 이동", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun LobbyPlayerCard(avatar: String, name: String, slot: String, ready: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
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
                Text(slot, color = UnoChessColors.Muted, fontSize = 10.sp)
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
private fun RuleLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("•", color = UnoChessColors.Lime, modifier = Modifier.padding(end = 7.dp))
        Text(text, color = UnoChessColors.Muted, fontSize = 12.sp)
    }
}
