package com.example.unochessprototype

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.unochessprototype.ui.navigation.MainBottomBar
import com.example.unochessprototype.ui.theme.UnoChessTheme

class MainActivity : FragmentActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationCompose: ComposeView
    private var selectedRootDestination by mutableIntStateOf(R.id.homeFragment)

    private val rootDestinations = setOf(
        R.id.homeFragment,
        R.id.localGameFragment,
        R.id.onlineFragment,
        R.id.profileSettingsFragment,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        bottomNavigationCompose = findViewById(R.id.bottom_navigation_compose)
        bottomNavigationCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        bottomNavigationCompose.setContent {
            UnoChessTheme {
                MainBottomBar(
                    selectedDestinationId = selectedRootDestination,
                    onDestinationSelected = ::navigateToRoot,
                )
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in rootDestinations) {
                selectedRootDestination = destination.id
                bottomNavigationCompose.visibility = View.VISIBLE
            } else {
                bottomNavigationCompose.visibility = View.GONE
            }
        }
    }

    private fun navigateToRoot(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return

        navController.popBackStack(R.id.homeFragment, false)
        if (destinationId != R.id.homeFragment) {
            navController.navigate(destinationId)
        }
    }
}
