package com.example.unochessprototype.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import com.example.unochessprototype.data.LocalPreferences
import com.example.unochessprototype.data.LocalProfile
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.ui.screen.ProfileSettingsTabScreen

class ProfileSettingsFragment : Fragment() {

    private lateinit var preferences: LocalPreferences
    private var profile by mutableStateOf(LocalProfile())
    private var settings by mutableStateOf(LocalSettings())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = LocalPreferences(requireContext())
        reload()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = composeView {
        ProfileSettingsTabScreen(
            profile = profile,
            settings = settings,
            onSave = { updatedProfile, updatedSettings ->
                preferences.saveProfile(updatedProfile)
                preferences.saveSettings(updatedSettings)
                profile = updatedProfile
                settings = updatedSettings
            },
        )
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        profile = preferences.loadProfile()
        settings = preferences.loadSettings()
    }
}
