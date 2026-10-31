package com.example.unochessprototype.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.unochessprototype.data.LocalProfile

@Composable
fun LocalLobbyScreen(
    profile: LocalProfile,
    opponentName: String,
    onOpponentNameChange: (String) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    var player2Ready by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("게임 대기실", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("로컬 2인 플레이", color = Color(0xFFAAB8AE), fontSize = 13.sp)
            }
            OutlinedButton(onClick = onBack) { Text("나가기") }
        }

        PlayerLobbyCard(
            avatar = profile.avatar,
            name = profile.nickname,
            side = "WHITE · PLAYER 1",
            ready = true,
        )

        Text("VS", modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFFA8E85A), fontWeight = FontWeight.Black)

        PlayerLobbyCard(
            avatar = "♜",
            name = opponentName.ifBlank { "PLAYER 2" },
            side = "BLACK · PLAYER 2",
            ready = player2Ready,
        )

        OutlinedTextField(
            value = opponentName,
            onValueChange = onOpponentNameChange,
            label = { Text("PLAYER 2 닉네임") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF102219))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("게임 규칙", fontWeight = FontWeight.Black, color = Color(0xFFA8E85A))
                Text("• 손패 5장 고정", fontSize = 13.sp)
                Text("• 숫자 1~8 → A~H 열", fontSize = 13.sp)
                Text("• JOKER → 원하는 이동 가능 열", fontSize = 13.sp)
                Text("• KING 직접 포획 시 승리", fontSize = 13.sp)
                Text("• 캐슬링/앙파상/체크메이트 없음", fontSize = 13.sp)
            }
        }

        Spacer(Modifier.weight(1f))

        if (!player2Ready) {
            Button(
                onClick = { player2Ready = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("PLAYER 2 준비 완료")
            }
        } else {
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                Text("게임 시작", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun PlayerLobbyCard(
    avatar: String,
    name: String,
    side: String,
    ready: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102219)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF173126), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(avatar, fontSize = 30.sp)
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(name, fontWeight = FontWeight.Black)
                Text(side, fontSize = 12.sp, color = Color(0xFFAAB8AE))
            }
            Text(
                if (ready) "READY" else "WAITING",
                color = if (ready) Color(0xFFA8E85A) else Color(0xFFF2C94C),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
            )
        }
    }
}
