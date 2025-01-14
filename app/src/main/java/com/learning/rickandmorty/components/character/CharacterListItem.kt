package com.learning.rickandmorty.components.character

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.learning.network.models.domain.Character
import com.learning.rickandmorty.components.common.CharacterImage
import com.learning.rickandmorty.components.common.CharacterNameComponent
import com.learning.rickandmorty.components.common.DataPoint
import com.learning.rickandmorty.components.common.DataPointComponent
import com.learning.rickandmorty.ui.theme.RickAction

@Composable
fun CharacterListItem(
    modifier: Modifier,
    character: Character,
    characterDataPoints: List<DataPoint>,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(140.dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(Color.Transparent, RickAction)),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                onClick()
            }
    ) {
        Box {
            CharacterImage(
                imageUrl = character.imageUrl,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
            )

            CharacterStatusCircle(
                status = character.status,
                modifier = Modifier.padding(start = 6.dp, top = 6.dp)
            )
        }
        LazyHorizontalGrid(rows = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            content = {
            items(
                items = listOf(DataPoint(title = "Name", description = character.name))+characterDataPoints,
                key = { it.hashCode() }
            ) { dataPoint ->
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(end = 16.dp)
                ) {
                    DataPointComponent(dataPoint = sanitize(dataPoint))
                }
            }
        })
    }
}

private fun sanitize(dataPoint: DataPoint) : DataPoint {

    if(dataPoint.description.length > 14 ) {
        return dataPoint.copy(description = (dataPoint.description.take(12) + ".."))
    }

    return dataPoint
}