package com.example.unochessprototype.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalProfile
import com.example.unochessprototype.game.FirstTurnDecider
import com.example.unochessprototype.game.LocalPlayerSlot

private val DrawPanel = Color(0xFF102219)
private val DrawPanel2 = Color(0xFF173126)
private val DrawLime = Color(0xFFA8E85A)
private val DrawMuted = Color(0xFFAAB8AE)
private val DrawBorder = Color(0xFF2D4A38)

@Composable
fun FirstTurnDrawScreen(
    profile: LocalProfile,
    opponentName: String,
    onDecided: (player1IsWhite: Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val decider = remember { FirstTurnDecider() }
    var player1Card by remember { mutableStateOf<Int?>(null) }
    var player2Card by remember { mutableStateOf<Int?>(null) }

    val result = if (player1Card != null && player2Card != null) {
        decider.decide(player1Card!!, player2Card!!)
    } else {
        null
    }

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
                Text("선공 결정", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(
                    "1~8 중 높은 숫자를 뽑은 플레이어가 WHITE · 선공",
                    color = DrawMuted,
                    fontSize = 12.sp,
                )
            }
            OutlinedButton(onClick = onBack) { Text("대기실") }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DrawPanel),
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                DrawPlayerRow(
                    label = "PLAYER 1",
                    name = profile.nickname,
                    card = player1Card,
                    canDraw = player1Card == null && result == null,
                    onDraw = { player1Card = decider.drawCard() },
                )

                Text("VS", color = DrawLime, fontWeight = FontWeight.Black, fontSize = 18.sp)

                DrawPlayerRow(
                    label = "PLAYER 2",
                    name = opponentName.ifBlank { "PLAYER 2" },
                    card = player2Card,
                    canDraw = player1Card != null && player2Card == null && result == null,
                    onDraw = { player2Card = decider.drawCard() },
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DrawPanel2),
        ) {
            val message = when {
                player1Card == null -> "PLAYER 1이 먼저 숫자 카드를 뽑으세요."
                player2Card == null -> "PLAYER 2가 숫자 카드를 뽑으세요."
                result?.isTie == true -> "동점입니다. 두 플레이어 모두 다시 뽑습니다."
                result?.winner == LocalPlayerSlot.PLAYER1 -> "${profile.nickname} 선공 · WHITE 배정"
                result?.winner == LocalPlayerSlot.PLAYER2 -> "${opponentName.ifBlank { "PLAYER 2" }} 선공 · WHITE 배정"
                else -> "선공을 결정합니다."
            }

            Text(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            "※ 선공 결정용 숫자 카드는 게임용 40장 카드 덱과 별도로 사용되며, 실제 손패나 드로우 덱 수에는 영향을 주지 않습니다.",
            color = DrawMuted,
            fontSize = 11.sp,
        )

        Spacer(Modifier.weight(1f))

        when {
            result?.isTie == true -> {
                Button(
                    onClick = {
                        player1Card = null
                        player2Card = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DrawLime),
                ) {
                    Text("다시 뽑기", color = Color(0xFF0B160D), fontWeight = FontWeight.Black)
                }
            }

            result?.winner != null -> {
                Button(
                    onClick = { onDecided(result.winner == LocalPlayerSlot.PLAYER1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DrawLime),
                ) {
                    Text("게임 시작", color = Color(0xFF0B160D), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun DrawPlayerRow(
    label: String,
    name: String,
    card: Int?,
    canDraw: Boolean,
    onDraw: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = DrawLime, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text(name, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 88.dp)
                .background(
                    if (card == null) Color(0xFF0D2C1C) else Color(0xFFF0F0E6),
                    RoundedCornerShape(10.dp),
                )
                .border(
                    2.dp,
                    if (card == null) DrawLime.copy(alpha = 0.55f) else DrawBorder,
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = card?.toString() ?: "?",
                color = if (card == null) DrawLime else Color(0xFF132018),
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
            )
        }

        Button(onClick = onDraw, enabled = canDraw) {
            Text(if (card == null) "뽑기" else "완료")
        }
    }
}
