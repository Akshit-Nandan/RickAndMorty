package com.learning.rickandmorty.components.character

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.learning.network.models.domain.CharacterStatus
import com.learning.rickandmorty.ui.theme.RickAction

@Composable
fun CharacterDetailsNamePlateComponent(name: String, status: CharacterStatus) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        CharacterStatusComponent(characterStatus = status)
        Text(
            text = name,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = RickAction
        )
    }
}

@Preview
@Composable
internal fun CharacterDetailsNamePlateComponentPreview() {
    CharacterDetailsNamePlateComponent(name = "Rick Sanchez",
        status = CharacterStatus.Alive)
}