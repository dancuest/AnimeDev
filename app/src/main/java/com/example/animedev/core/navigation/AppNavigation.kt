package com.example.animedev.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Quiz // Importación CORRECTA
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Unificado a Icons.Filled por consistencia
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Favorites : Screen("favorites", "Favoritos", Icons.Filled.Favorite)
    // Corregido para usar Outlined, que es donde reside el icono Quiz
    object Trivia : Screen("trivia", "Trivias", Icons.Outlined.Quiz) // Uso CORRECTO
    object Settings : Screen("settings", "Ajustes", Icons.Filled.Settings)
    object Profile : Screen("profile", "Perfil", Icons.Filled.AccountCircle)
    object AnimeDetail : Screen("anime/{animeId}", "Detalle", Icons.Filled.Home) {
        fun createRoute(animeId: Long) = "anime/$animeId"
    }
}