package com.learning.rickandmorty.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.learning.network.models.domain.Character
import com.learning.network.models.domain.CharacterPage
import com.learning.network.models.domain.CharacterStatus
import com.learning.rickandmorty.components.character.CharacterListItem
import com.learning.rickandmorty.components.common.BasicToolBar
import com.learning.rickandmorty.components.common.DataPoint
import com.learning.rickandmorty.components.common.LoadingState
import com.learning.rickandmorty.ui.theme.RickAction
import com.learning.rickandmorty.ui.theme.RickPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import javax.inject.Inject

sealed interface SearchScreenViewState {
    data object Searching : SearchScreenViewState
    data object Empty : SearchScreenViewState
    data class Content(
        val userQuery: String,
        val results: List<Character>,
        val filterState: FilterState,
    ) : SearchScreenViewState {
        data class FilterState(
            val statuses: List<CharacterStatus>,
            val selectedStatuses: List<CharacterStatus>,
        )
    }

    data class Error(val message: String) : SearchScreenViewState
}


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    sealed interface SearchState {
        data class UserQuery(val query: String) : SearchState
        data object Empty : SearchState
    }

    val searchTextFieldState = TextFieldState()

    private val _viewState =
        MutableStateFlow<SearchScreenViewState>(SearchScreenViewState.Error("Nothing Found"))
    val viewState = _viewState.asStateFlow()

    fun toggleStatus(status: CharacterStatus) {
        _viewState.update {
            val currentState = (it as? SearchScreenViewState.Content) ?: return@update it
            val currentSelectedStatuses = currentState.filterState.selectedStatuses
            val newStatuses = if (currentSelectedStatuses.contains(status)) {
                currentSelectedStatuses - status
            } else {
                currentSelectedStatuses + status
            }
            return@update currentState.copy(
                filterState = currentState.filterState.copy(
                    selectedStatuses = newStatuses
                )
            )
        }
    }

    private fun searchAllCharacters(searchQuery: String) = viewModelScope.launch {
        _viewState.update { SearchScreenViewState.Searching }
        characterRepository.fetchAllCharactersByName(searchQuery = searchQuery)
            .onSuccess { characters ->
                val allStatuses =
                    characters.map { it.status }.toSet().toList().sortedBy { it.displayName }
                _viewState.update {
                    SearchScreenViewState.Content(
                        results = characters,
                        userQuery = searchQuery,
                        filterState = SearchScreenViewState.Content.FilterState(
                            statuses = allStatuses,
                            selectedStatuses = allStatuses
                        )
                    )
                }
            }.onFailure { exception ->
                _viewState.update {
                    SearchScreenViewState.Error(
                        message = "No search results found!"
                    )
                }
            }
    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchTextState: StateFlow<SearchState> = snapshotFlow {
        searchTextFieldState.text
    }.debounce(200).mapLatest { value ->
        if (value.isBlank()) SearchState.Empty else SearchState.UserQuery(
            query = value.toString()
        )
    }.stateIn(
        initialValue = SearchState.Empty,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 2000)
    )

    fun observerUserSearch() = viewModelScope.launch {
        searchTextState.collectLatest { searchState ->
            when (searchState) {
                is SearchState.Empty -> _viewState.update { SearchScreenViewState.Empty }
                is SearchState.UserQuery -> searchAllCharacters(searchQuery = searchState.query)
            }
        }
    }
}

@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    onCharacterClick: (Int) -> Unit,
) {

    val viewState by searchViewModel.viewState.collectAsStateWithLifecycle()

    DisposableEffect(
        key1 = Unit
    ) {
        val job = searchViewModel.observerUserSearch()

        onDispose {
            job.cancel()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        BasicToolBar(title = "Search")
        AnimatedVisibility(visible = viewState is SearchScreenViewState.Searching) {
            LinearProgressIndicator(
                modifier = Modifier
                    .height(6.dp)
                    .fillMaxWidth(),
                color = RickAction
            )

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search Icon",
                    tint = RickPrimary
                )
                BasicTextField(
                    state = searchViewModel.searchTextFieldState, modifier = Modifier.weight(1f)
                )
            }
            AnimatedVisibility(visible = searchViewModel.searchTextFieldState.text.isNotBlank()) {
                Icon(imageVector = Icons.Rounded.Delete,
                    contentDescription = "Clear Button",
                    tint = RickAction,
                    modifier = Modifier.clickable {
                        searchViewModel.searchTextFieldState.edit {
                            delete(0, length)
                        }
                    })
            }
        }

        when (val state = viewState) {
            is SearchScreenViewState.Empty -> {
                Text(
                    text = "Search for character",
                    color = Color.White,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = TextAlign.Center
                )

            }

            SearchScreenViewState.Searching -> {
//                LoadingState()
            }

            is SearchScreenViewState.Content -> {
                Column {
                    Text(
                        text = "${state.results.size} results for \'${state.userQuery}\'",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 4.dp)
                    )
                    Row (
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        state.filterState.statuses.forEach { status ->
                            val isSelected = state.filterState.selectedStatuses.contains(status)
                            val contentColor = if (isSelected) RickAction else Color.LightGray
                            val count = state.results.count { it.status === status }
                            Row(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = contentColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        searchViewModel.toggleStatus(status)
                                    }
                                    .clip(RoundedCornerShape(8.dp))

                            ) {
                                Text(
                                    text = "$count",
                                    modifier = Modifier
                                        .background(color = contentColor)
                                        .padding(6.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = status.displayName,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(6.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    Box {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = 16.dp, end = 16.dp, bottom = 24.dp
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filteredResult = state.results.filter {
                                state.filterState.selectedStatuses.contains(it.status)
                            }
                            items(items = filteredResult) { character ->
                                CharacterListItem(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .animateItem(),
                                    character = character,
                                    characterDataPoints = buildList {
                                        add(
                                            DataPoint(
                                                title = "Last known location",
                                                description = character.location.name
                                            )
                                        )
                                        add(
                                            DataPoint(
                                                title = "Species", description = character.species
                                            )
                                        )
                                        add(
                                            DataPoint(
                                                title = "Gender",
                                                description = character.gender.displayName
                                            )
                                        )
                                        character.type.takeIf { it.isNotEmpty() }?.let { type ->
                                            add(DataPoint(title = "Type", description = type))
                                        }
                                        add(
                                            DataPoint(
                                                title = "Origin",
                                                description = character.origin.name
                                            )
                                        )
                                        add(
                                            DataPoint(
                                                title = "Episode Count",
                                                description = character.episodeIds.size.toString()
                                            )
                                        )

                                    }) {
                                    onCharacterClick(character.id)
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            RickPrimary, Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            is SearchScreenViewState.Error -> {
                Column {
                    Text(
                        text = state.message,
                        color = Color.White,
                        fontSize = 32.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

    }
}