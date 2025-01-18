package com.learning.rickandmorty.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.network.models.domain.Character
import com.learning.network.models.domain.CharacterPage
import com.learning.rickandmorty.components.character.CharacterGridItem
import com.learning.rickandmorty.components.common.BasicToolBar
import com.learning.rickandmorty.components.common.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    val repository: CharacterRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow<HomeScreenViewState>(HomeScreenViewState.Loading)
    val viewState: StateFlow<HomeScreenViewState> = _viewState.asStateFlow()

    private val fetchedCharacterPages = mutableListOf<CharacterPage>()

    fun fetchInitialPage() = viewModelScope.launch {
        if (fetchedCharacterPages.isNotEmpty()) return@launch
        repository.fetchCharacterPage(page = 0).onSuccess { characterPage ->
            fetchedCharacterPages.add(characterPage)
            _viewState.update {
                 HomeScreenViewState.GridDisplay(characters = characterPage.results)
            }
        }.onFailure {
            // TODO
        }
    }

    fun fetchNextPage() = viewModelScope.launch {
        val nextPageIndex = fetchedCharacterPages.size + 1
        fetchedCharacterPages.lastOrNull()?.let { lastPage ->
            if (nextPageIndex > lastPage.info.pages)
                return@launch
        }
        repository.fetchCharacterPage(page = nextPageIndex).onSuccess { characterPage ->
            fetchedCharacterPages.add(characterPage)
            _viewState.update { currentState ->
                val currentCharacters =
                    (currentState as? HomeScreenViewState.GridDisplay)?.characters ?: emptyList()

                return@update HomeScreenViewState.GridDisplay(characters = currentCharacters + characterPage.results)
            }
        }.onFailure {
            // TODO
        }
    }

}

sealed interface HomeScreenViewState {
    data object Loading : HomeScreenViewState
    data class Error(val message: String) : HomeScreenViewState
    data class GridDisplay(val characters: List<Character>) : HomeScreenViewState
}

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onCharacterSelected: (Int) -> Unit,
) {

    val viewState by viewModel.viewState.collectAsState()

    LaunchedEffect(key1 = viewModel) {
        viewModel.fetchInitialPage()
    }

    val scrollState = rememberLazyGridState()
    val fetchNextPage: Boolean by remember {
        derivedStateOf {
            val currentCharacterCount =
                (viewState as? HomeScreenViewState.GridDisplay)?.characters?.size
                    ?: return@derivedStateOf false

            val lastDisplayedIndex =
                scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            return@derivedStateOf currentCharacterCount - lastDisplayedIndex <= 10
        }
    }

    LaunchedEffect(key1 = fetchNextPage, block = {
        if (fetchNextPage) viewModel.fetchNextPage()
    })

    when (val state = viewState) {
        is HomeScreenViewState.Loading -> {
            LoadingState()
        }

        is HomeScreenViewState.GridDisplay -> {
            Column {
                BasicToolBar(title = "All Characters", onBackAction = null)
                LazyVerticalGrid(
                    state = scrollState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp),
                    columns = GridCells.Fixed(2)
                ) {
                    items(items = state.characters, key = { it.hashCode() }) { character ->
                        CharacterGridItem(modifier = Modifier, character = character) {
                            onCharacterSelected(character.id)
                        }
                    }
                }
            }
        }

        is HomeScreenViewState.Error -> TODO()
    }


}

