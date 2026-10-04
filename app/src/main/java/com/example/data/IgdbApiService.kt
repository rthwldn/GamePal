package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GameSearchResult(
    val title: String,
    val platform: String,
    val coverUrl: String,
    val description: String,
    val releaseYear: Int,
    val genres: String,
    val developer: String
)

class GameMetadataService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun searchGames(query: String): List<GameSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext PreloadedGames.CATALOG.take(12).map { it.toSearchResult() }
        }

        // 1. Search in our curated offline database first
        val localMatches = PreloadedGames.search(trimmed).map { it.toSearchResult() }

        // 2. Query open online video game search (RAWG / Open DB / Steam search)
        val onlineMatches = mutableListOf<GameSearchResult>()
        try {
            val encodedQuery = java.net.URLEncoder.encode(trimmed, "UTF-8")
            // Try open search endpoint
            val request = Request.Builder()
                .url("https://api.rawg.io/api/games?key=c542e67aec3a4340908f9de9e86038af&search=$encodedQuery&page_size=10")
                .header("User-Agent", "PlayPulse-Android-App")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val results = json.optJSONArray("results") ?: JSONArray()
                    for (i in 0 until results.length()) {
                        val item = results.getJSONObject(i)
                        val name = item.optString("name", "")
                        if (name.isNotBlank()) {
                            val background = item.optString("background_image", "")
                            val released = item.optString("released", "")
                            val year = released.take(4).toIntOrNull() ?: 2020
                            
                            val genresArray = item.optJSONArray("genres") ?: JSONArray()
                            val genresList = mutableListOf<String>()
                            for (g in 0 until genresArray.length()) {
                                genresList.add(genresArray.getJSONObject(g).optString("name", ""))
                            }

                            val platformsArray = item.optJSONArray("platforms") ?: JSONArray()
                            var bestPlatform = "PC / Steam"
                            if (platformsArray.length() > 0) {
                                val pObj = platformsArray.getJSONObject(0).optJSONObject("platform")
                                val pName = pObj?.optString("name", "") ?: ""
                                bestPlatform = when {
                                    pName.contains("PlayStation 5", true) -> "PlayStation 5"
                                    pName.contains("PlayStation 4", true) -> "PlayStation 4"
                                    pName.contains("PlayStation 3", true) -> "PlayStation 3"
                                    pName.contains("PlayStation 2", true) -> "PlayStation 2"
                                    pName.contains("Xbox Series", true) -> "Xbox Series X|S"
                                    pName.contains("Xbox One", true) -> "Xbox One"
                                    pName.contains("Xbox 360", true) -> "Xbox 360"
                                    pName.contains("Switch", true) -> "Nintendo Switch"
                                    pName.contains("Wii", true) -> "Nintendo Wii"
                                    pName.contains("PC", true) -> "PC (Steam / GOG / Epic)"
                                    pName.contains("iOS", true) || pName.contains("Android", true) -> "Mobile (iOS / Android)"
                                    else -> pName.ifBlank { "PlayStation 5" }
                                }
                            }

                            onlineMatches.add(
                                GameSearchResult(
                                    title = name,
                                    platform = bestPlatform,
                                    coverUrl = background,
                                    description = "Populární titul $name ($year). Žánr: ${genresList.joinToString(", ").ifEmpty { "Akční adventura" }}.",
                                    releaseYear = year,
                                    genres = genresList.joinToString(", ").ifEmpty { "Akční adventura" },
                                    developer = "Game Studio"
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Silently fallback to local / fallback search
        }

        // Merge local matches and online matches, eliminating duplicates by title
        val combined = mutableListOf<GameSearchResult>()
        val seenTitles = mutableSetOf<String>()

        for (item in localMatches) {
            val key = item.title.lowercase().trim()
            if (seenTitles.add(key)) {
                combined.add(item)
            }
        }

        for (item in onlineMatches) {
            val key = item.title.lowercase().trim()
            if (seenTitles.add(key)) {
                combined.add(item)
            }
        }

        // If still empty, construct an intelligent result for the custom query
        if (combined.isEmpty()) {
            combined.add(
                GameSearchResult(
                    title = trimmed,
                    platform = "PlayStation 5",
                    coverUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=600&q=80",
                    description = "Vlastní titul: $trimmed",
                    releaseYear = 2024,
                    genres = "Videohra",
                    developer = "Vlastní záznam"
                )
            )
        }

        return@withContext combined
    }

    private fun PreloadedGameData.toSearchResult(): GameSearchResult {
        return GameSearchResult(
            title = this.title,
            platform = this.platform,
            coverUrl = this.coverUrl,
            description = this.description,
            releaseYear = this.releaseYear,
            genres = this.genres,
            developer = this.developer
        )
    }
}
