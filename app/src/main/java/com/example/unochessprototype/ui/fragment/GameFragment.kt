package com.example.unochessprototype.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.unochessprototype.R
import com.example.unochessprototype.data.LocalPreferences
import com.example.unochessprototype.game.GameResult
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.ui.screen.ReleaseGameScreen
import com.example.unochessprototype.viewmodel.GameViewModel
import com.example.unochessprototype.viewmodel.LocalGameSessionViewModel

class GameFragment : Fragment() {

    private val session: LocalGameSessionViewModel by activityViewModels()
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val preferences = LocalPreferences(requireContext())

        return composeView {
            val profile = preferences.loadProfile()
            val settings = preferences.loadSettings()

            ReleaseGameScreen(
                game = gameViewModel.game,
                profile = profile,
                opponentName = session.opponentName,
                settings = settings,
                player1IsWhite = session.player1IsWhite,
                onExit = { goHome() },
                onRematch = { rematch() },
                onGameFinished = { result -> recordResult(preferences, result) },
            )
        }
    }

    private fun recordResult(preferences: LocalPreferences, result: GameResult) {
        if (!gameViewModel.markResultRecorded()) return

        val profile = preferences.loadProfile()
        val player1Side = if (session.player1IsWhite) Player.WHITE else Player.BLACK
        val updated = when (result) {
            is GameResult.Win -> profile.copy(
                games = profile.games + 1,
                wins = profile.wins + if (result.winner == player1Side) 1 else 0,
            )
            is GameResult.Draw -> profile.copy(
                games = profile.games + 1,
                draws = profile.draws + 1,
            )
        }
        preferences.saveProfile(updated)
    }

    private fun rematch() {
        val nav = findNavController()
        nav.popBackStack(R.id.localGameFragment, false)
        nav.navigate(R.id.firstTurnFragment)
    }

    private fun goHome() {
        session.prepareNewMatch()
        val nav = findNavController()
        if (!nav.popBackStack(R.id.homeFragment, false)) {
            nav.navigate(R.id.homeFragment)
        }
    }
}
