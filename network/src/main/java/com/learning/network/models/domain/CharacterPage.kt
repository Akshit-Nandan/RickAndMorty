package com.learning.network.models.domain

import com.learning.network.models.remote.RemoteCharacter
import kotlinx.serialization.Serializable


data class CharacterPage(
    val info : Info,
    val results : List<Character>
) {
    data class Info (
        val count : Int,
        val pages : Int,
        val next : String?,
        val prev : String?,
    )
}

