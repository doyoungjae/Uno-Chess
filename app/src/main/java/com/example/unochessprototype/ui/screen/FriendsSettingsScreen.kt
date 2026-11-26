
package com.example.unochessprototype.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.viewmodel.FriendsViewModel

@Composable
fun FriendsSettingsScreen(
    friends: FriendsViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var friendId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← 설정", color = UnoChessColors.Lime)
        }

        Text(
            text = "친구",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = UnoChessColors.Text
        )

        Text(
            text = "친구를 추가하고 함께 플레이하세요",
            fontSize = 12.sp,
            color = UnoChessColors.Muted
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = UnoChessColors.Surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "친구 추가",
                    color = UnoChessColors.Text,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = friendId,
                    onValueChange = { friendId = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("친구 UID") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        friends.request(friendId)
                        friendId = ""
                    },
                    enabled = friendId.isNotBlank() && !friends.busy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UnoChessColors.Lime
                    )
                ) {
                    Text("친구 요청 보내기")
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "받은 친구 요청 (${friends.incoming.size})",
            color = UnoChessColors.Text,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(friends.incoming.toList(), key = { it.first }) { entry ->
                val key = entry.first
                val request = entry.second

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = UnoChessColors.Surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "UID: ${request.sender}",
                            color = UnoChessColors.Text
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { friends.respond(key, true) },
                                enabled = !friends.busy
                            ) {
                                Text("수락")
                            }

                            TextButton(
                                onClick = { friends.respond(key, false) },
                                enabled = !friends.busy
                            ) {
                                Text("거절", color = UnoChessColors.Lime)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "내 친구 (${friends.friendIds.size})",
                    color = UnoChessColors.Text,
                    fontWeight = FontWeight.Bold
                )
            }

            items(friends.friendIds.toList(), key = { it }) { friendUid ->
                val presence = friends.people[friendUid]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = UnoChessColors.Surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = presence?.name?.takeIf { it.isNotBlank() }
                                    ?: friendUid,
                                color = UnoChessColors.Text,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = friendUid,
                                color = UnoChessColors.Muted,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = if (
                                presence?.sessions?.values?.any { it } == true
                            ) "온라인" else "오프라인",
                            color = UnoChessColors.Lime,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        friends.error?.let { message ->
            Text(
                text = message,
                color = UnoChessColors.Lime,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
