package com.example.unochessprototype.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.unochessprototype.R
import com.example.unochessprototype.data.LocalPreferences
import com.example.unochessprototype.data.LocalProfile
import com.example.unochessprototype.ui.screen.HomeTabScreen

class HomeFragment : Fragment() {

    private var profile by mutableStateOf(LocalProfile())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = composeView {
        HomeTabScreen(
            profile = profile,
            onLocalGame = { findNavController().navigate(R.id.localGameFragment) },
            onOnlineGame = { findNavController().navigate(R.id.onlineFragment) },
            onProfile = { findNavController().navigate(R.id.profileSettingsFragment) },
        )
    }

    override fun onResume() {
        super.onResume()
        profile = LocalPreferences(requireContext()).loadProfile()
    }
}
