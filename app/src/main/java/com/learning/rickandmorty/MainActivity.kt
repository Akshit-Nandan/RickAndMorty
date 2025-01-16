package com.learning.rickandmorty

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


import com.learning.network.KtorClient
import com.learning.rickandmorty.screens.AllEpisodesScreen
import com.learning.rickandmorty.screens.CharacterDetailsScreen
import com.learning.rickandmorty.screens.CharacterEpisodeScreen
import com.learning.rickandmorty.screens.HomeScreen
import com.learning.rickandmorty.ui.theme.RickAction
import com.learning.rickandmorty.ui.theme.RickAndMortyTheme
import com.learning.rickandmorty.ui.theme.RickPrimary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class NavDestination(val title: String, val route: String, val icon: ImageVector) {
    data object Home :
        NavDestination(title = "Home", route = "home_screen", icon = Icons.Filled.Home)

    data object Episodes :
        NavDestination(title = "Episodes", route = "episodes", icon = Icons.Filled.PlayArrow)

    data object Search :
        NavDestination(title = "Search", route = "search", icon = Icons.Filled.Search)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var ktorClient: KtorClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()
            val topLevelRoutes = listOf(
                NavDestination.Home,
                NavDestination.Episodes,
                NavDestination.Search,
            )

            var selectedIndex by remember {
                mutableIntStateOf(0)
            }

            RickAndMortyTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = RickPrimary
                        ) {
                            topLevelRoutes.forEachIndexed{ screen, topLevelRoute ->
                                NavigationBarItem(
                                    icon = { Icon(topLevelRoute.icon, contentDescription = null) },
                                    label = { Text(topLevelRoute.title) },
                                    modifier = Modifier.background(color = RickPrimary),
                                    selected = selectedIndex == screen,
                                    onClick = {
                                        navController.navigate(topLevelRoute.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            // on the back stack as users select items
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // reselecting the same item
                                            launchSingleTop = true
                                            // Restore state when reselecting a previously selected item
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = RickAction,
                                        selectedTextColor = RickAction,
                                        indicatorColor = Color.Transparent
                                    )
                                )

                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home_screen",
                        modifier = Modifier
                            .background(color = RickPrimary)
                            .padding(innerPadding)
                    ) {
                        composable(route = "home_screen") {
                            selectedIndex = 0
                            HomeScreen { characterId ->
                                navController.navigate("character_details/$characterId")
                            }
                        }
                        composable("character_details/{character_Id}", arguments = listOf(
                            navArgument(name = "character_Id") { type = NavType.IntType }
                        )) { backStackEntry ->
                            val characterId: Int =
                                backStackEntry.arguments?.getInt("character_Id") ?: -1
                            CharacterDetailsScreen(
                                characterId = characterId,
                                onBackAction = {
                                    navController.navigateUp()
                                }
                            ) {
                                navController.navigate("character_episodes/$it")
                            }
                        }
                        composable(
                            "character_episodes/{character_Id}",
                            arguments = listOf(navArgument("character_Id") {
                                type = NavType.IntType
                            })
                        ) { backStackEntry ->
                            val characterId: Int =
                                backStackEntry.arguments?.getInt("character_Id") ?: -1
                            CharacterEpisodeScreen(
                                characterId = characterId,
                                ktorClient = ktorClient
                            ) {
                                navController.navigateUp()
                            }
                        }
                        composable(route = NavDestination.Episodes.route) {
                            selectedIndex = 1
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AllEpisodesScreen()
                            }
                        }
                        composable(route = NavDestination.Search.route) {
                            selectedIndex = 2
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Search", fontSize = 62.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

