package com.example.campergas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.campergas.ui.screens.home.HomeScreen
import com.example.campergas.ui.screens.weight.WeightScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Weight.route) {
            WeightScreen(navController = navController)
        }
        

    }
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Weight : Screen("weight")
    data object Inclination : Screen("inclination")
    data object Consumption : Screen("consumption")
    data object BleConnect : Screen("ble_connect")
    data object Settings : Screen("settings")
    data object CaravanConfig : Screen("caravan_config")
}
