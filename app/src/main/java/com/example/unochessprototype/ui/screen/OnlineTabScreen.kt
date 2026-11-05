package com.example.unochessprototype.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.ui.theme.UnoChessColors

@Composable
fun OnlineTabScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text("온라인 게임", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Firebase 연동 예정", color = UnoChessColors.Muted, fontSize = 12.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier.size(72.dp).background(UnoChessColors.SurfaceRaised, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.CloudQueue, contentDescription = null, tint = UnoChessColors.Lime, modifier = Modifier.size(36.dp))
                }
                Text("ONLINE MODE", fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(
                    "로컬 게임과 게임 엔진을 먼저 안정화한 뒤, Firebase Authentication과 실시간 게임 상태 동기화를 연결합니다.",
                    color = UnoChessColors.Muted,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                )
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("구현 예정", fontWeight = FontWeight.Black, color = UnoChessColors.Lime)
                Text("• 사용자 로그인 / 고유 UID", color = UnoChessColors.Muted, fontSize = 12.sp)
                Text("• 방 생성 / 초대 코드 / 입장", color = UnoChessColors.Muted, fontSize = 12.sp)
                Text("• 턴 및 보드 상태 실시간 동기화", color = UnoChessColors.Muted, fontSize = 12.sp)
                Text("• 재접속 / 경기 결과 저장", color = UnoChessColors.Muted, fontSize = 12.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = UnoChessColors.Warning)
                Text("  현재 버전에서는 온라인 매칭을 실행하지 않습니다.", color = UnoChessColors.Muted, fontSize = 12.sp)
            }
        }
    }
}
