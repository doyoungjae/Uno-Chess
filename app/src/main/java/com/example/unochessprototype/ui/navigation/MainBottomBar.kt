package com.example.unochessprototype.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.unochessprototype.R
import com.example.unochessprototype.ui.theme.UnoChessColors

private data class BottomDestination(
    val destinationId: Int,
    val label: String,
    val icon: @Composable () -> Unit,
)

@Composable
fun MainBottomBar(
    selectedDestinationId: Int,
    onDestinationSelected: (Int) -> Unit,
) {
    val items = listOf(
        BottomDestination(R.id.homeFragment, "홈") {
            Icon(Icons.Rounded.Home, contentDescription = null)
        },
        BottomDestination(R.id.localGameFragment, "로컬") {
            Icon(Icons.Rounded.SportsEsports, contentDescription = null)
        },
        BottomDestination(R.id.onlineFragment, "온라인") {
            Icon(Icons.Rounded.Public, contentDescription = null)
        },
        BottomDestination(R.id.profileSettingsFragment, "프로필") {
            Icon(Icons.Rounded.Person, contentDescription = null)
        },
    )

    NavigationBar(
        containerColor = UnoChessColors.Surface,
        contentColor = UnoChessColors.Text,
    ) {
        items.forEach { item ->
            val selected = selectedDestinationId == item.destinationId
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(item.destinationId) },
                icon = item.icon,
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF0B160D),
                    selectedTextColor = UnoChessColors.Lime,
                    indicatorColor = UnoChessColors.Lime,
                    unselectedIconColor = UnoChessColors.Muted,
                    unselectedTextColor = UnoChessColors.Muted,
                ),
            )
        }
    }
}
