package com.learning.network.models.remote

import com.learning.network.models.domain.Character
import com.learning.network.models.domain.CharacterGender
import com.learning.network.models.domain.CharacterStatus
import kotlinx.serialization.Serializable


@Serializable
data class RemoteCharacter(
    val created: String,
    val episode: List<String>,
    val gender: String,
    val id: Int,
    val image: String,
    val location: Location,
    val name: String,
    val origin: Origin,
    val species: String,
    val status: String,
    val type: String,
    val url: String,
) {
    @Serializable
    data class Location(
        val name: String,
        val url: String,
    )

    @Serializable
    data class Origin(
        val name: String,
        val url: String,
    )
}

fun RemoteCharacter.toDomainCharacter(): Character {
    val characterGender = when (gender.lowercase()) {
        "male" -> CharacterGender.Male
        "female" -> CharacterGender.Female
        "genderless" -> CharacterGender.Genderless
        else -> CharacterGender.Unknown
    }
    val characterStatus = when (status.lowercase()) {
        "alive" -> CharacterStatus.Alive
        "dead" -> CharacterStatus.Dead
        else -> CharacterStatus.Unknown
    }
    return Character(
        created = created,
        episodeIds = episode.map { it.substring(it.lastIndexOf('/') + 1).toInt() },
        gender = characterGender,
        id = id,
        imageUrl = image,
        location = Character.Location(name = location.name, url = location.url),
        name = name,
        origin = Character.Origin(name = origin.name, url = origin.url),
        species = species,
        type = type,
        url = url,
        status = characterStatus
    )
}
