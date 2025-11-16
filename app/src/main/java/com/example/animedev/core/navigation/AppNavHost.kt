package com.example.animedev.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.animedev.feature.favorites.ui.FavoritesScreen
import com.example.animedev.feature.home.ui.HomeScreen
import com.example.animedev.feature.profile.ui.ProfileScreen
import com.example.animedev.feature.settings.ui.SettingsScreen
import com.example.animedev.feature.trivia.ui.TriviaScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Favorites.route) { FavoritesScreen() }
        composable(Screen.Trivia.route) { TriviaScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }
    }
}
