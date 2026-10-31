package com.example.unochessprototype.ui

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
fun ProfileScreen(
    profile: LocalProfile,
    onSave: (LocalProfile) -> Unit,
    onBack: () -> Unit,
) {
    var nickname by remember { mutableStateOf(profile.nickname) }
    var avatar by remember { mutableStateOf(profile.avatar) }
    val avatars = listOf("♞", "♜", "♛", "♝", "♚", "♟")

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
            Text("로컬 프로필", fontSize = 22.sp, fontWeight = FontWeight.Black)
            OutlinedButton(onClick = onBack) { Text("뒤로") }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF102219)),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .background(Color(0xFF173126), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(avatar, fontSize = 48.sp)
                }
                Spacer(Modifier.height(14.dp))
                Text("사용자 아이콘", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    avatars.forEach { candidate ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (candidate == avatar) Color(0xFFA8E85A) else Color(0xFF173126),
                                    CircleShape,
                                )
                                .clickable { avatar = candidate },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(candidate, fontSize = 25.sp, color = if (candidate == avatar) Color(0xFF0B160D) else Color.White)
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it.take(16) },
            label = { Text("닉네임") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard("게임", profile.games.toString(), Modifier.weight(1f))
            StatCard("승리", profile.wins.toString(), Modifier.weight(1f))
            StatCard("무승부", profile.draws.toString(), Modifier.weight(1f))
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                onSave(profile.copy(nickname = nickname.ifBlank { "PLAYER 1" }, avatar = avatar))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("저장")
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF173126)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFA8E85A))
            Text(label, fontSize = 12.sp, color = Color(0xFFAAB8AE))
        }
    }
}
