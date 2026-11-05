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
import com.example.unochessprototype.ui.theme.UnoChessColors

object AppRoute {
    const val HOME = "home"
    const val LOCAL = "local"
    const val ONLINE = "online"
    const val PROFILE = "profile"
    const val FIRST_TURN = "first_turn"
    const val GAME = "game"

    val bottomRoutes = setOf(HOME, LOCAL, ONLINE, PROFILE)
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)

@Composable
fun MainBottomBar(
    currentRoute: String?,
    onDestinationSelected: (String) -> Unit,
) {
    val items = listOf(
        BottomDestination(AppRoute.HOME, "홈") {
            Icon(Icons.Rounded.Home, contentDescription = null)
        },
        BottomDestination(AppRoute.LOCAL, "로컬") {
            Icon(Icons.Rounded.SportsEsports, contentDescription = null)
        },
        BottomDestination(AppRoute.ONLINE, "온라인") {
            Icon(Icons.Rounded.Public, contentDescription = null)
        },
        BottomDestination(AppRoute.PROFILE, "프로필") {
            Icon(Icons.Rounded.Person, contentDescription = null)
        },
    )

    NavigationBar(
        containerColor = UnoChessColors.Surface,
        contentColor = UnoChessColors.Text,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(item.route) },
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
