package com.learning.rickandmorty.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.learning.rickandmorty.ui.theme.RickAction
import com.learning.rickandmorty.ui.theme.RickTextPrimary

data class DataPoint (
    val title : String,
    val description : String
)

@Composable
fun DataPointComponent(dataPoint : DataPoint) {
    Column{
        Text(
            text = dataPoint.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = RickAction
        )
        Text(
            text = dataPoint.description,
            fontSize = 18.sp,
            color = RickTextPrimary
        )
    }
}

@Preview
@Composable
fun DataPointComponentPreview() {
    DataPointComponent(dataPoint = DataPoint(title = "Title", description = "This is description."))
}