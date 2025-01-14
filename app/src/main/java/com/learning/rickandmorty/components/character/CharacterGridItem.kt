package com.learning.rickandmorty.components.character

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learning.network.models.domain.Character
import com.learning.network.models.domain.CharacterGender
import com.learning.network.models.domain.CharacterStatus
import com.learning.rickandmorty.components.common.CharacterImage

import com.learning.rickandmorty.ui.theme.RickAction

@Composable
fun CharacterGridItem(
    modifier: Modifier,
    character: Character,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
//            .width(150.dp)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color.Transparent,RickAction)),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(
               RoundedCornerShape(12.dp)
            )
            .clickable {
                onClick()
            }
    ) {
        Box {
            CharacterImage(imageUrl = character.imageUrl)

            CharacterStatusCircle(
                status = character.status,
                modifier = Modifier.padding(start = 6.dp, top = 6.dp)
            )
        }
        Text(
            modifier = modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            text = character.name,
            fontSize = 26.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
            color = RickAction
        )
    }
}

@Preview
@Composable
private fun CharacterGridItemPreview() {
    CharacterGridItem(
        modifier = Modifier.fillMaxWidth(),
        character = Character(
            created = "timestamp",
            episodeIds = listOf(1, 2, 3, 4, 5),
            gender = CharacterGender.Male,
            id = 123,
            imageUrl = "https://rickandmortyapi.com/api/character/avatar/2.jpeg",
            location = Character.Location(
                name = "Earth",
                url = ""
            ),
            name = "Morty Smith",
            origin = Character.Origin(
                name = "Earth",
                url = ""
            ),
            species = "Human",
            status = CharacterStatus.Alive,
            type = "",
            url = "",
        ),
        onClick = {}
    )
}