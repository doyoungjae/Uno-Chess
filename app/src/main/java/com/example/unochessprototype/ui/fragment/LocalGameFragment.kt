package com.example.unochessprototype.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.unochessprototype.R
import com.example.unochessprototype.data.LocalPreferences
import com.example.unochessprototype.data.LocalProfile
import com.example.unochessprototype.ui.screen.LocalGameTabScreen
import com.example.unochessprototype.viewmodel.LocalGameSessionViewModel

class LocalGameFragment : Fragment() {

    private val session: LocalGameSessionViewModel by activityViewModels()

    private var profile by mutableStateOf(
        LocalProfile()
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = composeView {

        LocalGameTabScreen(
            profile = profile,
            opponentName = session.opponentName,
            player2Ready = session.player2Ready,

            onOpponentNameChange = session::updateOpponentName,

            onReadyChange = session::updatePlayer2Ready,

            onStart = {
                findNavController()
                    .navigate(R.id.firstTurnFragment)
            },
        )
    }

    override fun onResume() {
        super.onResume()

        profile =
            LocalPreferences(
                requireContext()
            ).loadProfile()
    }
}