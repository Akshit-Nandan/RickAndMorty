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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.learning.network.ApiOperation
import com.learning.network.KtorClient
import com.learning.network.models.domain.Character
import com.learning.rickandmorty.components.character.CharacterDetailsNamePlateComponent
import com.learning.rickandmorty.components.character.CharacterGridItem
import com.learning.rickandmorty.components.character.CharacterListItem
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
                        msg = exception.message ?: "Unknown Error"
                    )
                }
            }
    }


}

sealed interface CharacterDetailsViewState {
    object Loading : CharacterDetailsViewState
    data class Error(val msg: String) : CharacterDetailsViewState
    data class Success(
        val character: Character,
        val characterDataPoint: List<DataPoint>,
    ) : CharacterDetailsViewState
}

@Composable
fun CharacterDetailsScreen(
    characterId: Int,
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
    onEpisodeClicked: (Int) -> Unit,
) {

    LaunchedEffect(key1 = Unit, block = {
        delay(500)
        viewModel.fetchCharacter(characterId = characterId)
    })

    val state by viewModel.state.collectAsState()

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
                    LazyRow {
                        repeat(10) {
                            item {Spacer(Modifier.width(16.dp))}
                            item {
                                CharacterGridItem(
                                    modifier = Modifier,
                                    character = viewState.character
                                ) {

                                }
                            }
                        }
                    }
                }

                item {  Spacer(Modifier.height(16.dp)) }

                repeat(10) {
                    item { Spacer(Modifier.height(16.dp)) }
                    item {
                        CharacterListItem(
                            modifier = Modifier,
                            character = viewState.character,
                            characterDataPoints = viewState.characterDataPoint
                        ) {
                            
                        }
                    }
                }

                return@LazyColumn

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
