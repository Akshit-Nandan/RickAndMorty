package com.learning.network

import android.util.Log
import com.learning.network.models.domain.Character
import com.learning.network.models.domain.CharacterPage
import com.learning.network.models.domain.Episode
import com.learning.network.models.domain.EpisodePage
import com.learning.network.models.remote.RemoteCharacter
import com.learning.network.models.remote.RemoteCharacterPage
import com.learning.network.models.remote.RemoteEpisode
import com.learning.network.models.remote.RemoteEpisodePage
import com.learning.network.models.remote.toDomainCharacter
import com.learning.network.models.remote.toDomainCharacterPage
import com.learning.network.models.remote.toDomainEpisode
import com.learning.network.models.remote.toDomainEpisodePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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

    private suspend fun getEpisodesByPage(pageId: Int): ApiOperation<EpisodePage> {
        return safeApiCall<EpisodePage> {
            client.get(urlString = "episode", block = {
                url {
                    parameters.append("page", pageId.toString())
                }
            }).body<RemoteEpisodePage>().toDomainEpisodePage()
        }
    }

    suspend fun getAllEpisodes(): ApiOperation<List<Episode>> {
        val data = mutableListOf<Episode>()
        var exception: Exception? = null

        getEpisodesByPage(pageId = 1).onSuccess { firstPageResult ->
            val totalPage = firstPageResult.info.pages
            data.addAll(firstPageResult.results)
            repeat(totalPage - 1) { id ->
                getEpisodesByPage(pageId = id + 2).onSuccess { nextPage ->
                    data.addAll(nextPage.results)
                }.onFailure {
                    exception = it
                }
                if (exception != null) return@onSuccess
            }
        }.onFailure {
            exception = it
        }

        return exception?.let {
            ApiOperation.Failure<List<Episode>>(exception = it)
        } ?: ApiOperation.Success<List<Episode>>(data = data)

    }

    suspend fun getCharacterByPage(
        pageNumber: Int,
        queryParams: Map<String, String>,
    ): ApiOperation<CharacterPage> {
        return safeApiCall {
            client
                .get(urlString = "character") {
                    url {
                        parameters.append("page", pageNumber.toString())
                        queryParams.forEach { mapEntry ->
                            parameters.append(mapEntry.key, mapEntry.value)
                        }
                    }
                }
                .body<RemoteCharacterPage>()
                .toDomainCharacterPage()
        }
    }

    suspend fun getAllCharactersByName(searchQuery : String): ApiOperation<List<Character>> {
        val data = mutableListOf<Character>()
        var exception: Exception? = null

        getCharacterByPage(pageNumber = 1, queryParams = mapOf("name" to searchQuery)).onSuccess { firstPageResult ->
            val totalPage = firstPageResult.info.pages
            data.addAll(firstPageResult.results)
            repeat(totalPage - 1) { id ->
                getCharacterByPage(pageNumber = id + 2, queryParams = mapOf("name" to searchQuery)).onSuccess { nextPage ->
                    data.addAll(nextPage.results)
                }.onFailure {
                    exception = it
                }
                if (exception != null) return@onSuccess
            }
        }.onFailure {
            exception = it
        }

        return exception?.let {
            ApiOperation.Failure<List<Character>>(exception = it)
        } ?: ApiOperation.Success<List<Character>>(data = data)

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

    suspend fun onSuccess(block: suspend (T) -> Unit): ApiOperation<T> {
        if (this is Success) block(data)
        return this
    }

    fun onFailure(block: (Exception) -> Unit): ApiOperation<T> {
        if (this is Failure) block(exception)
        return this
    }
}