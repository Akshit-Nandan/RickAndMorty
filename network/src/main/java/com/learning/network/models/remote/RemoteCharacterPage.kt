package com.learning.network.models.remote

import com.learning.network.models.domain.CharacterPage
import kotlinx.serialization.Serializable

@Serializable
data class RemoteCharacterPage(
    val info : Info,
    val results : List<RemoteCharacter>
) {
    @Serializable
    data class Info (
        val count : Int,
        val pages : Int,
        val next : String?,
        val prev : String?,
    )
}

fun RemoteCharacterPage.toDomainCharacterPage() : CharacterPage {
    return CharacterPage(
        info = CharacterPage.Info(
            count = info.count,
            pages = info.pages,
            next = info.next,
            prev = info.prev,
        ),
        results = results.map { it.toDomainCharacter() }
    )
}