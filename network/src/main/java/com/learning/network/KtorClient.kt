package com.learning.network

import android.util.Log
import com.learning.network.models.domain.Character
import com.learning.network.models.domain.Episode
import com.learning.network.models.remote.RemoteCharacter
import com.learning.network.models.remote.RemoteEpisode
import com.learning.network.models.remote.toDomainCharacter
import com.learning.network.models.remote.toDomainEpisode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


class KtorClient {
    private val client = HttpClient(OkHttp) {
        defaultRequest { url("https://rickandmortyapi.com/api/") }

        install(Logging) {
            logger = Logger.SIMPLE
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private var characterCache = mutableMapOf<Int, Character>()

    suspend fun getCharacter(id: Int): ApiOperation<Character> {
        characterCache[id]?.let {
            return ApiOperation.Success<Character>(data = it);
        }
        return safeApiCall<Character> {
            client.get("character/$id").body<RemoteCharacter>().toDomainCharacter().also {
                characterCache[id] = it
            }
        }
    }

    suspend fun getEpisodes(episodeIds: List<Int>): ApiOperation<List<Episode>> {
        val idsCommaSeparated = episodeIds.joinToString(",") { it.toString() }.plus(",")
        return safeApiCall<List<Episode>> {
            client.get("episode/$idsCommaSeparated").body<List<RemoteEpisode>>()
                .map { it.toDomainEpisode() }
        }
    }

    private inline fun <T> safeApiCall(apiCall: () -> T): ApiOperation<T> {
        return try {
            ApiOperation.Success(data = apiCall())
        } catch (e: Exception) {
            ApiOperation.Failure(exception = e)
        }
    }
}

sealed interface ApiOperation<T> {
    data class Success<T>(val data: T) : ApiOperation<T>
    data class Failure<T>(val exception: Exception) : ApiOperation<T>

    fun onSuccess(block: (T) -> Unit): ApiOperation<T> {
        if (this is Success) block(data)
        return this
    }

    fun onFailure(block: (Exception) -> Unit): ApiOperation<T> {
        if (this is Failure) block(exception)
        return this
    }
}