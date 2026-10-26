package org.ucb.appp1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class Film(
    val title: String,
    val episode: Int,
    val director: String,
    val releaseDate: String,
    val openingCrawl: String
)

data class GithubUser(val login: String, val name: String?, val bio: String?)

object StarWarsService {
    suspend fun films(): List<Film> = withContext(Dispatchers.IO) {
        val json = JSONObject(readJson("https://swapi.dev/api/films/"))
        val results = json.getJSONArray("results")
        (0 until results.length()).map { index ->
            val film = results.getJSONObject(index)
            Film(
                title = film.getString("title"),
                episode = film.getInt("episode_id"),
                director = film.getString("director"),
                releaseDate = film.getString("release_date"),
                openingCrawl = film.getString("opening_crawl")
            )
        }.sortedBy { it.episode }
    }
}

object GithubService {
    suspend fun find(alias: String): GithubUser = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(alias, "UTF-8")
        val json = JSONObject(readJson("https://api.github.com/users/$encoded"))
        GithubUser(
            login = json.getString("login"),
            name = json.optString("name").takeUnless { it.isBlank() || it == "null" },
            bio = json.optString("bio").takeUnless { it.isBlank() || it == "null" }
        )
    }
}

private fun readJson(address: String): String {
    val connection = URL(address).openConnection() as HttpURLConnection
    connection.connectTimeout = 10_000
    connection.readTimeout = 10_000
    connection.setRequestProperty("Accept", "application/json")
    try {
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("HTTP ${connection.responseCode}")
        }
        return connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}
