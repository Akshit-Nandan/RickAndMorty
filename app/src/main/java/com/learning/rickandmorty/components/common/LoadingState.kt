package com.learning.rickandmorty.components.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learning.rickandmorty.ui.theme.RickAction

private val defaultModifier = Modifier
    .fillMaxSize()
    .padding(128.dp)

@Composable
fun LoadingState(modifier: Modifier = defaultModifier) {
    CircularProgressIndicator(
        modifier = modifier,
        color = RickAction,
    )
}