package com.learning.rickandmorty.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.SubcomposeAsyncImage
import com.learning.network.ApiOperation
import com.learning.network.KtorClient
import com.learning.network.models.domain.Character
import com.learning.network.models.domain.CharacterPage
import com.learning.rickandmorty.components.character.CharacterDetailsNamePlateComponent
import com.learning.rickandmorty.components.common.BasicToolBar
import com.learning.rickandmorty.components.common.DataPoint
import com.learning.rickandmorty.components.common.DataPointComponent
import com.learning.rickandmorty.components.common.LoadingState
import com.learning.rickandmorty.ui.theme.RickAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CharacterRepository @Inject constructor(private val ktorClient: KtorClient) {

    suspend fun fetchCharacter(characterId: Int): ApiOperation<Character> {
        return ktorClient.getCharacter(characterId)
    }

    suspend fun fetchCharacterPage(page: Int): ApiOperation<CharacterPage> {
        return ktorClient.getCharacterByPage(pageNumber = page)
    }
}

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
) : ViewModel() {
    private val _internalStorageFlow = MutableStateFlow<CharacterDetailsViewState>(
        value = CharacterDetailsViewState.Loading
    )
    val state = _internalStorageFlow.asStateFlow()

    fun fetchCharacter(characterId: Int) = viewModelScope.launch {
        characterRepository.fetchCharacter(characterId = characterId)
            .onSuccess { character ->
                _internalStorageFlow.update {
                    return@update CharacterDetailsViewState.Success(
                        character = character,
                        characterDataPoint = buildList {
                            add(
                                DataPoint(
                                    title = "Last known location",
                                    description = character.location.name
                                )
                            )
                            add(DataPoint(title = "Species", description = character.species))
                            add(
                                DataPoint(
                                    title = "Gender",
                                    description = character.gender.displayName
                                )
                            )
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
                    )
                }
            }.onFailure { exception ->
                _internalStorageFlow.update {
                    return@update CharacterDetailsViewState.Error(
                        message = exception.message ?: "Unknown Error"
                    )
                }
            }
    }


}

sealed interface CharacterDetailsViewState {
    object Loading : CharacterDetailsViewState
    data class Error(val message: String) : CharacterDetailsViewState
    data class Success(
        val character: Character,
        val characterDataPoint: List<DataPoint>,
    ) : CharacterDetailsViewState
}

@Composable
fun CharacterDetailsScreen(
    characterId: Int,
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
    onBackAction: () -> Unit,
    onEpisodeClicked: (Int) -> Unit,
) {

    LaunchedEffect(key1 = Unit, block = {
        delay(500)
        viewModel.fetchCharacter(characterId = characterId)
    })

    val state by viewModel.state.collectAsState()

    Column {
        BasicToolBar(title = "Character Details", onBackAction = onBackAction)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(all = 16.dp)

        ) {

            when (val viewState = state) {
                is CharacterDetailsViewState.Loading -> {
                    item { LoadingState() }
                }

                is CharacterDetailsViewState.Error -> {
                    item {

                    }
                }

                is CharacterDetailsViewState.Success -> {
                    item {
                        CharacterDetailsNamePlateComponent(
                            name = viewState.character.name,
                            status = viewState.character.status
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Image
                    item {
                        SubcomposeAsyncImage(
                            model = viewState.character.imageUrl,
                            contentDescription = "Character Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(shape = RoundedCornerShape(12.dp)),
                            loading = { LoadingState() }
                        )
                    }

                    items(viewState.characterDataPoint) {
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
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                                .border(
                                    width = 1.dp,
                                    color = RickAction,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onEpisodeClicked(characterId)
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
