package com.learning.rickandmorty.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.learning.network.KtorClient
import com.learning.network.models.domain.Character
import com.learning.network.models.remote.toDomainCharacter
import com.learning.rickandmorty.components.character.CharacterDetailsNamePlateComponent
import com.learning.rickandmorty.components.common.DataPoint
import com.learning.rickandmorty.components.common.DataPointComponent
import com.learning.rickandmorty.ui.theme.RickAction
import kotlinx.coroutines.delay

@Composable
fun CharacterDetailsScreen(
    ktorClient: KtorClient,
    characterId: Int,
) {
    var character by remember { mutableStateOf<Character?>(null) }

    val characterDataPoints: List<DataPoint> by remember {
        derivedStateOf {
            buildList {
                character?.let { character ->
                    add(
                        DataPoint(
                            title = "Last known location",
                            description = character.location.name
                        )
                    )
                    add(DataPoint(title = "Species", description = character.species))
                    add(DataPoint(title = "Gender", description = character.gender.displayName))
                    character.type.takeIf { it.isNotEmpty() }?.let { type ->
                        add(DataPoint(title = "Type", description = type))
                    }
                    add(DataPoint(title = "Origin", description = character.origin.name))
                    add(
                        DataPoint(
                            title = "Episode Count",
                            description = character.episodeIds.size.toString()
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit, block = {
        delay(500)
        ktorClient.getCharacter(characterId)
            .onSuccess {
                character = it
            }.onFailure {

            }
    })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp)

    ) {

        if (character == null) {
            item {
                LoadingState()
            }
            return@LazyColumn
        }

        item {
            CharacterDetailsNamePlateComponent(name = character!!.name, status = character!!.status)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Image
        item {
            SubcomposeAsyncImage(
                model = character!!.imageUrl, contentDescription = "Character Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(shape = RoundedCornerShape(12.dp)),
                loading = { LoadingState() }
            )
        }

        items(characterDataPoints) {
            Spacer(modifier = Modifier.height(32.dp))
            DataPointComponent(dataPoint = it)
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text(
                text = "View all episodes",
                color = RickAction,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .border(
                        width = 1.dp,
                        color = RickAction,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { }
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun LoadingState() {
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 128.dp),
        color = RickAction,
    )
}