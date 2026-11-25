package com.example.unochessprototype.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.unochessprototype.data.LocalPreferences
import com.example.unochessprototype.ui.navigation.AppRoute
import com.example.unochessprototype.ui.navigation.MainBottomBar
import com.example.unochessprototype.ui.screen.*
import com.example.unochessprototype.ui.screen.CustomizeScreen
import com.example.unochessprototype.ui.screen.HomeTabScreen
import com.example.unochessprototype.ui.screen.LocalGameTabScreen
import com.example.unochessprototype.ui.screen.OnlineGameScreen
import com.example.unochessprototype.ui.screen.OnlineLobbyScreen
import com.example.unochessprototype.ui.screen.OnlineTabScreen
import com.example.unochessprototype.ui.screen.ProfileSettingsTabScreen
import com.example.unochessprototype.ui.screen.ReleaseGameScreen
import com.example.unochessprototype.ui.screen.SplashScreen
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.ui.theme.UnoChessTheme
import com.example.unochessprototype.viewmodel.GameViewModel
import com.example.unochessprototype.viewmodel.LocalGameSessionViewModel
import com.example.unochessprototype.viewmodel.OnlineBattleViewModel
import com.example.unochessprototype.viewmodel.UserProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun UnoChessApp() {
    val context = LocalContext.current
    val preferences = remember(context.applicationContext) {
        LocalPreferences(context.applicationContext)
    }
    var settings by remember { mutableStateOf(preferences.loadSettings()) }

    val profileViewModel: UserProfileViewModel = viewModel()
    val profileState = profileViewModel.uiState
    val profile = profileState.profile

    val friends: com.example.unochessprototype.viewmodel.FriendsViewModel = viewModel()
    LaunchedEffect(profile.uid, profile.nickname) {
        if (profile.uid.isNotBlank()) { friends.connect(profile.uid); friends.name(profile.nickname) }
    }
    val localSession: LocalGameSessionViewModel = viewModel()
    val onlineBattle: OnlineBattleViewModel = viewModel()
    val onlineState = onlineBattle.uiState
    LaunchedEffect(profile.uid, profileState.isConnected) {
        if (profileState.isConnected && profile.uid.isNotBlank()) {
            onlineBattle.observeWaitingRooms(profile.uid)
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in AppRoute.bottomRoutes

    fun navigateToRoot(route: String) {
        if (navController.currentDestination?.route == route) return
        if (route == AppRoute.HOME && navController.popBackStack(AppRoute.HOME, false)) return

        navController.navigate(route) {
            popUpTo(AppRoute.HOME) {
                inclusive = false
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun returnHomeFromOnline() {
        onlineBattle.stopObservingRoom()
        navigateToRoot(AppRoute.HOME)
    }

    fun goHomeClearingLocalMatch() {
        localSession.prepareNewMatch()
        navigateToRoot(AppRoute.HOME)
    }

    // 앱을 처음 켤 때만 스플래시를 보여준다(화면 회전 시 다시 뜨지 않음). 그동안 뒤에서 Firebase 연결이 진행된다.
    var showSplash by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1700)
        showSplash = false
    }

    UnoChessTheme(boardTheme = settings.boardTheme) {
    Box(modifier = Modifier.fillMaxSize()) {
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
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            composable(AppRoute.HOME) {
                HomeTabScreen(
                    profile = profile,
                    profileConnected = profileState.isConnected,
                    settings = settings,
                    onCustomize = { navController.navigate(AppRoute.CUSTOMIZE) { launchSingleTop = true } },
                    onProfile = { navigateToRoot(AppRoute.PROFILE) },
                )
            }

            composable(AppRoute.CUSTOMIZE) {
                CustomizeScreen(
                    settings = settings,
                    onSettingsChange = { updated ->
                        settings = updated
                        preferences.saveSettings(updated)
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.LOCAL) {
                BackHandler(onBack = ::goHomeClearingLocalMatch)
                LocalGameTabScreen(
                    profile = profile,
                    opponentName = localSession.opponentName,
                    player2Ready = localSession.player2Ready,
                    onOpponentNameChange = localSession::setOpponentName,
                    onReadyChange = localSession::setPlayer2Ready,
                    onStart = { navController.navigate(AppRoute.GAME) },
                )
            }

            composable(AppRoute.ONLINE) {
                OnlineTabScreen(
                    profile = profile,
                    connected = profileState.isConnected,
                    rooms = onlineState.rooms,
                    loadingRooms = onlineState.loadingRooms,
                    busy = onlineState.busy,
                    errorMessage = onlineState.errorMessage,
                    onBackToHome = { navigateToRoot(AppRoute.HOME) },
                    friends = friends,
                    onCreateRoom = { roomName ->
                        onlineBattle.createRoom(profile, roomName) { roomId ->
                            navController.navigate(AppRoute.onlineLobby(roomId))
                        }
                    },
                    onJoinRoom = { roomId ->
                        onlineBattle.joinRoom(roomId, profile) { joinedRoomId ->
                            navController.navigate(AppRoute.onlineLobby(joinedRoomId)) { launchSingleTop = true }
                        }
                    },
                )
            }

            composable(AppRoute.PROFILE) {
                BackHandler { navigateToRoot(AppRoute.HOME) }
                SettingsMainScreen { category -> navController.navigate("settings/" + category.name) }
            }
            composable("settings/SOUND") {
                SoundSettingsScreen(settings, { settings = it; preferences.saveSettings(it) }, { navController.popBackStack() })
            }
            composable("settings/FRIENDS") {
                FriendsSettingsScreen(friends, { navController.popBackStack() })
            }
            composable("settings/STATISTICS") {
                StatisticsSettingsScreen(profile, { navController.popBackStack() })
            }
            composable("settings/RATE_APP") {
                RateAppSettingsScreen { navController.popBackStack() }
            }
            composable("settings/PROFILE") {
                ProfileSettingsTabScreen(
                    profile = profile,
                    profileLoading = profileState.isLoading,
                    profileError = profileState.errorMessage,
                    saveMessage = profileState.saveMessage,
                    settings = settings,
                    onSaveOnlineIdentity = profileViewModel::updateIdentity,
                    onSaveSettings = { updatedSettings ->
                        settings = updatedSettings
                        preferences.saveSettings(updatedSettings)
                    },
                    onRetryProfile = profileViewModel::connect,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.GAME) {
                val gameViewModel: GameViewModel = viewModel()
                ReleaseGameScreen(
                    game = gameViewModel.game,
                    profile = profile,
                    opponentName = localSession.opponentName,
                    settings = settings,
                    onExit = ::goHomeClearingLocalMatch,
                    onRematch = {
                        navController.popBackStack(AppRoute.LOCAL, false)
                        navController.navigate(AppRoute.GAME)
                    },
                    onGameFinished = { gameViewModel.markResultRecorded() },
                )
            }

            composable(AppRoute.ONLINE_LOBBY) { entry ->
                val roomId = entry.arguments?.getString("roomId").orEmpty()
                LaunchedEffect(roomId, profile.uid) {
                    if (roomId.isNotBlank() && profile.uid.isNotBlank()) {
                        onlineBattle.observeRoom(roomId, profile.uid)
                    }
                }

                OnlineLobbyScreen(
                    onRetry = { onlineBattle.retryRoom(roomId, profile.uid) },
                    room = onlineState.currentRoom,
                    profile = profile,
                    busy = onlineState.busy,
                    errorMessage = onlineState.errorMessage,
                    onBack = {
                        onlineBattle.leaveLobby(roomId, profile.uid) {
                            returnHomeFromOnline()
                        }
                    },
                    onReadyChange = { ready -> onlineBattle.setReady(roomId, profile.uid, ready) },
                    onStartGame = {
                        onlineBattle.startGame(roomId, profile.uid) { }
                    },
                    onGameStarted = {
                        navController.navigate(AppRoute.onlineGame(roomId)) {
                            popUpTo(AppRoute.ONLINE) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(AppRoute.ONLINE_GAME) { entry ->
                val roomId = entry.arguments?.getString("roomId").orEmpty()
                LaunchedEffect(roomId, profile.uid) {
                    if (roomId.isNotBlank() && profile.uid.isNotBlank()) {
                        onlineBattle.observeRoom(roomId, profile.uid)
                    }
                }

                OnlineGameScreen(
                    serverNow = onlineBattle::serverNow,
                    onClockExpired = { onlineBattle.checkClock(roomId, profile.uid) },
                    room = onlineState.currentRoom,
                    profile = profile,
                    settings = settings,
                    busy = onlineState.busy,
                    errorMessage = onlineState.errorMessage,
                    onMove = { cardId, move, jokerFile ->
                        onlineBattle.playMove(roomId, profile.uid, cardId, move, jokerFile)
                    },
                    onExchange = { cardIds ->
                        onlineBattle.exchangeCards(roomId, profile.uid, cardIds)
                    },
                    onForfeit = {
                        onlineBattle.leaveLobby(roomId, profile.uid) {
                            returnHomeFromOnline()
                        }
                    },
                    onExitFinished = { returnHomeFromOnline() },
                )
            }
        }
    }
    AnimatedVisibility(visible = showSplash, enter = EnterTransition.None, exit = fadeOut(tween(450))) {
        SplashScreen()
    }
    }
    }
}
