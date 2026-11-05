package com.example.unochessprototype.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.unochessprototype.R
import com.example.unochessprototype.data.LocalPreferences
import com.example.unochessprototype.ui.FirstTurnDrawScreen
import com.example.unochessprototype.viewmodel.LocalGameSessionViewModel

class FirstTurnFragment : Fragment() {

    private val session: LocalGameSessionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val profile = LocalPreferences(requireContext()).loadProfile()
        return composeView {
            Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                FirstTurnDrawScreen(
                profile = profile,
                opponentName = session.opponentName,
                onDecided = { player1IsWhite ->
                    session.setFirstPlayer(player1IsWhite)
                    findNavController().navigate(R.id.gameFragment)
                },
                    onBack = { findNavController().popBackStack() },
                )
            }
        }
    }
}
