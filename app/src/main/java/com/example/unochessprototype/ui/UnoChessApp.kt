package com.example.unochessprototype.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.unochessprototype.data.LocalPreferences
import com.example.unochessprototype.game.GameResult
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.ui.navigation.AppRoute
import com.example.unochessprototype.ui.navigation.MainBottomBar
import com.example.unochessprototype.ui.screen.HomeTabScreen
import com.example.unochessprototype.ui.screen.LocalGameTabScreen
import com.example.unochessprototype.ui.screen.OnlineTabScreen
import com.example.unochessprototype.ui.screen.ProfileSettingsTabScreen
import com.example.unochessprototype.ui.screen.ReleaseGameScreen
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.viewmodel.GameViewModel
import com.example.unochessprototype.viewmodel.LocalGameSessionViewModel

@Composable
fun UnoChessApp() {
    val context = LocalContext.current
    val preferences = remember(context.applicationContext) {
        LocalPreferences(context.applicationContext)
    }

    var profile by remember { mutableStateOf(preferences.loadProfile()) }
    var settings by remember { mutableStateOf(preferences.loadSettings()) }

    val session: LocalGameSessionViewModel = viewModel()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in AppRoute.bottomRoutes

    fun navigateToRoot(route: String) {
        if (currentRoute == route) return

        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun goHomeClearingMatchStack() {
        session.prepareNewMatch()
        navController.navigate(AppRoute.HOME) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = UnoChessColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    currentRoute = currentRoute,
                    onDestinationSelected = ::navigateToRoot,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(AppRoute.HOME) {
                HomeTabScreen(
                    profile = profile,
                    onLocalGame = { navigateToRoot(AppRoute.LOCAL) },
                    onOnlineGame = { navigateToRoot(AppRoute.ONLINE) },
                    onProfile = { navigateToRoot(AppRoute.PROFILE) },
                )
            }

            composable(AppRoute.LOCAL) {
                LocalGameTabScreen(
                    profile = profile,
                    opponentName = session.opponentName,
                    player2Ready = session.player2Ready,
                    onOpponentNameChange = session::setOpponentName,
                    onReadyChange = session::setPlayer2Ready,
                    onStart = {
                        navController.navigate(AppRoute.FIRST_TURN)
                    },
                )
            }

            composable(AppRoute.ONLINE) {
                OnlineTabScreen()
            }

            composable(AppRoute.PROFILE) {
                ProfileSettingsTabScreen(
                    profile = profile,
                    settings = settings,
                    onSave = { updatedProfile, updatedSettings ->
                        profile = updatedProfile
                        settings = updatedSettings
                        preferences.saveProfile(updatedProfile)
                        preferences.saveSettings(updatedSettings)
                    },
                )
            }

            composable(AppRoute.FIRST_TURN) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(UnoChessColors.Background)
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                ) {
                    FirstTurnDrawScreen(
                        profile = profile,
                        opponentName = session.opponentName,
                        onDecided = { player1IsWhite ->
                            session.setFirstPlayer(player1IsWhite)
                            navController.navigate(AppRoute.GAME)
                        },
                        onBack = {
                            navController.popBackStack()
                        },
                    )
                }
            }

            composable(AppRoute.GAME) {
                val gameViewModel: GameViewModel = viewModel()

                ReleaseGameScreen(
                    game = gameViewModel.game,
                    profile = profile,
                    opponentName = session.opponentName,
                    settings = settings,
                    player1IsWhite = session.player1IsWhite,
                    onExit = ::goHomeClearingMatchStack,
                    onRematch = {
                        navController.popBackStack(AppRoute.LOCAL, false)
                        navController.navigate(AppRoute.FIRST_TURN)
                    },
                    onGameFinished = { result ->
                        if (gameViewModel.markResultRecorded()) {
                            val player1Side = if (session.player1IsWhite) {
                                Player.WHITE
                            } else {
                                Player.BLACK
                            }

                            val updatedProfile = when (result) {
                                is GameResult.Win -> profile.copy(
                                    games = profile.games + 1,
                                    wins = profile.wins + if (result.winner == player1Side) 1 else 0,
                                )

                                is GameResult.Draw -> profile.copy(
                                    games = profile.games + 1,
                                    draws = profile.draws + 1,
                                )
                            }

                            profile = updatedProfile
                            preferences.saveProfile(updatedProfile)
                        }
                    },
                )
            }
        }
    }
}
