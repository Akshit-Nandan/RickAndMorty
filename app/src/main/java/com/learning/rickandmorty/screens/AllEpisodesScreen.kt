package com.learning.rickandmorty.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.network.ApiOperation
import com.learning.network.KtorClient
import com.learning.network.models.domain.Episode
import com.learning.rickandmorty.components.common.BasicToolBar
import com.learning.rickandmorty.components.common.LoadingState
import com.learning.rickandmorty.components.episode.EpisodeRowComponent
import com.learning.rickandmorty.ui.theme.RickAction
import com.learning.rickandmorty.ui.theme.RickPrimary
import com.learning.rickandmorty.ui.theme.RickTextPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AllEpisodesViewState {
    data object Loading : AllEpisodesViewState
    data class Error(val message: String) : AllEpisodesViewState
    data class Success(
        val data: Map<Int, List<Episode>>,
    ) : AllEpisodesViewState
}

@HiltViewModel
class AllEpisodesViewModel @Inject constructor(val repository: EpisodeRepository) : ViewModel() {
    private val _episodesList = MutableStateFlow<AllEpisodesViewState>(AllEpisodesViewState.Loading)
    val episodesList = _episodesList.asStateFlow()

    fun refreshAllEpisodes() = viewModelScope.launch {
        _episodesList.update {
            AllEpisodesViewState.Loading
        }
        repository.fetchAllEpisodes().onSuccess { episodes ->
            _episodesList.update {
                AllEpisodesViewState.Success(
                    data = episodes.groupBy { it.seasonNumber }
                )
            }
        }.onFailure { exeption ->
            _episodesList.update {
                AllEpisodesViewState.Error(
                    message = exeption.message ?: "Failed to load episodes !"
                )
            }
        }
    }

}

class EpisodeRepository @Inject constructor(private val client: KtorClient) {
    suspend fun fetchAllEpisodes(): ApiOperation<List<Episode>> {
        return client.getAllEpisodes()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllEpisodesScreen(
    episodesViewModel: AllEpisodesViewModel = hiltViewModel(),
) {
    val uiState by episodesViewModel.episodesList.collectAsState()

    LaunchedEffect(key1 = Unit, block = {
        episodesViewModel.refreshAllEpisodes()
    })

    when (val state = uiState) {
        AllEpisodesViewState.Loading -> {
            LoadingState()
        }

        is AllEpisodesViewState.Error -> {
            // TODO
        }

        is AllEpisodesViewState.Success -> {
            BasicToolBar(title = "All Episodes") {
                
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.data.forEach { mapEntry ->
                    val uniqueCharacterCount =
                        mapEntry.value.flatMap { it.characterIdsInEpisode }.toSet().size

                    stickyHeader(key = mapEntry.key) {
                        Column (
                            modifier = Modifier.fillMaxWidth().background(color = RickPrimary)
                        ){

                            Text(
                                text = "Season ${mapEntry.key.toString()}",
                                fontSize = 32.sp,
                                color = RickTextPrimary
                            )
                            Text(
                                text = "$uniqueCharacterCount unique characters",
                                color = RickTextPrimary,
                                fontSize = 22.sp
                            )
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .padding(top = 4.dp)
                                    .background(
                                        color = RickAction,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }

                    mapEntry.value.forEach { episode ->
                        item {
                            EpisodeRowComponent(episode = episode)
                        }
                    }
                }
            }
        }
    }
}