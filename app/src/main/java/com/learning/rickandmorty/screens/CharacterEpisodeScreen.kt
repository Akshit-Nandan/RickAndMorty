package com.learning.rickandmorty.screens

import android.graphics.Paint
import android.util.Log
import android.widget.Space
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.learning.network.KtorClient
import com.learning.network.models.domain.Character
import com.learning.network.models.domain.Episode
import com.learning.rickandmorty.components.character.CharacterDetailsNamePlateComponent
import com.learning.rickandmorty.components.common.BasicToolBar
import com.learning.rickandmorty.components.common.CharacterImage
import com.learning.rickandmorty.components.common.CharacterNameComponent
import com.learning.rickandmorty.components.common.DataPoint
import com.learning.rickandmorty.components.common.DataPointComponent
import com.learning.rickandmorty.components.common.LoadingState
import com.learning.rickandmorty.components.episode.EpisodeRowComponent
import com.learning.rickandmorty.ui.theme.RickAction
import com.learning.rickandmorty.ui.theme.RickPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun CharacterEpisodeScreen(characterId: Int, ktorClient: KtorClient, onBackAction: () -> Unit) {
    var characterState by remember {
        mutableStateOf<Character?>(null)
    }

    var episodesState by remember { mutableStateOf<List<Episode>>(emptyList()) }

    LaunchedEffect(key1 = Unit, block = {
        ktorClient.getCharacter(characterId).onSuccess { character ->
            characterState = character
            launch {
                ktorClient.getEpisodes(character.episodeIds).onSuccess { episodes ->
                    episodesState = episodes
                }.onFailure {
                    // todo
                }
            }
        }.onFailure {
            // todo
        }
    })

    characterState?.let { character ->
        Column {
            BasicToolBar(title = "All Episodes", onBackAction = onBackAction)
            MainScreen(character = character, episodes = episodesState)
        }
    } ?: LoadingState()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainScreen(character: Character, episodes: List<Episode>) {
    val episodeBySeasonMap = episodes.groupBy { it.seasonNumber }
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item { CharacterNameComponent(name = character.name) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            LazyRow {
                episodeBySeasonMap.forEach { mapEntry ->
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = RickPrimary),
                        ) {
                            DataPointComponent(
                                dataPoint = DataPoint(
                                    title = "Season ${mapEntry.key}",
                                    description = "${mapEntry.value.size} ep"
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { CharacterImage(imageUrl = character.imageUrl) }
        item { Spacer(modifier = Modifier.height(32.dp)) }

        episodeBySeasonMap.forEach { mapEntry ->
            stickyHeader(key = mapEntry.key) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(color = RickPrimary)
                ) {
                    SeasonHeader(seasonNumber = mapEntry.key)
                }
            }
            items(mapEntry.value) { episode ->
                EpisodeRowComponent(episode = episode)
            }
        }
    }
}

@Composable
private fun SeasonHeader(seasonNumber: Int) {
    Text(
        text = "Season $seasonNumber",
        fontSize = 32.sp,
        color = RickAction,
        lineHeight = 32.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                shape = RoundedCornerShape(12.dp),
                width = 1.dp,
                color = RickAction
            )
            .padding(vertical = 4.dp)
    )
}