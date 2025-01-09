package com.learning.network.models.remote

import com.learning.network.models.domain.Episode
import kotlinx.serialization.Serializable

@Serializable
data class RemoteEpisode(
    val id : Int,
    val name : String,
    val air_date : String,
    val episode : String,
    val characters : List<String>,
)

fun RemoteEpisode.toDomainEpisode() : Episode {
    return Episode(
        id = id,
        name = name,
        seasonNumber = episode.filter { it.isDigit() }.take(2).toInt(),
        episodeNumber = episode.filter { it.isDigit() }.substring(2).toInt(),
        airDate = air_date,
        characterIdsInEpisode = characters.map { it.substringAfterLast('/') .toInt() }

    )
}
