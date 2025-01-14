package com.learning.rickandmorty

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


import com.learning.network.KtorClient
import com.learning.network.Test
import com.learning.rickandmorty.screens.CharacterDetailsScreen
import com.learning.rickandmorty.screens.CharacterEpisodeScreen
import com.learning.rickandmorty.ui.theme.RickAndMortyTheme
import com.learning.rickandmorty.ui.theme.RickPrimary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var ktorClient : KtorClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()

            RickAndMortyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = RickPrimary
                ) {
                    NavHost(navController = navController, startDestination = "character_details") {
                        composable("character_details") { CharacterDetailsScreen(
                            characterId = 4,
                        ) {
                            navController.navigate("character_episodes/$it")
                        } }
                        composable("character_episodes/{character_Id}",
                            arguments = listOf(navArgument("character_Id"){type = NavType.IntType})
                        ) {backStackEntry ->
                            val characterId : Int = backStackEntry.arguments?.getInt("character_Id") ?: -1
                            CharacterEpisodeScreen(characterId = characterId, ktorClient = ktorClient)
                        }
                    }
//                    CharacterDetailsScreen(ktorClient = ktorClient, characterId = 1)
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        println("Activity is paused")
    }

    override fun onResume() {
        super.onResume()
        println("Activity is resumed")
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RickAndMortyTheme {
        Greeting("Android")
    }
}