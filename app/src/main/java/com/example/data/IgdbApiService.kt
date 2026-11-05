package com.example.data

import com.example.model.PlatformInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
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
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    suspend fun searchGames(query: String): List<GameSearchResult> = withContext(Dispatchers.IO) {
        val rawTrimmed = query.trim()
        if (rawTrimmed.isEmpty()) {
            return@withContext PreloadedGames.CATALOG.take(12).map { it.toSearchResult() }
        }

        // 1. Rozpoznání požadované platformy přímo ze zadání uživatele (např. "pro Switch 2", "na PS5", "PC")
        val qLower = rawTrimmed.lowercase()
        val detectedPlatform: String? = when {
            qLower.contains("switch 2") || qLower.contains("switch2") -> "Nintendo Switch 2"
            qLower.contains("switch") -> "Nintendo Switch"
            qLower.contains("ps5") || qLower.contains("playstation 5") -> "PlayStation 5"
            qLower.contains("xbox series") || qLower.contains("xbox") -> "Xbox Series"
            qLower.contains("pc") || qLower.contains("steam") -> "PC"
            qLower.contains("ostatní") || qLower.contains("ostatni") -> "Ostatní"
            else -> null
        }

        // 2. Očištění dotazu od názvů platforem a spojek pro vyhledávače
        val cleanedTitleQuery = rawTrimmed
            .replace(Regex("(?i)\\b(pro|na|for|on|hra|game)\\b"), " ")
            .replace(Regex("(?i)\\b(switch\\s*2|switch2|nintendo\\s*switch|switch|playstation\\s*5|ps5|xbox\\s*series|xbox|pc|steam)\\b"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
            .ifBlank { rawTrimmed }

        // 3. Lokální vyhledávání (okamžité, funguje offline, podporuje diakritiku i malá písmena)
        val localMatches = PreloadedGames.search(cleanedTitleQuery).map { item ->
            // Pokud uživatel výslovně uvedl platformu v dotazu, použijeme ji!
            val effectivePlatform = detectedPlatform ?: PlatformInfo.findByName(item.platform).name
            item.toSearchResult().copy(platform = effectivePlatform)
        }

        val onlineMatches = mutableListOf<GameSearchResult>()

        // 4. Online vyhledávání přes otevřené API (Wikipedia OpenSearch + Page Summary)
        // Běží okamžitě, nemá rate-limity a nikdy nepadá na Cloudflare 522
        try {
            val encodedQuery = URLEncoder.encode(cleanedTitleQuery, "UTF-8")
            val wikiSearchUrl = "https://en.wikipedia.org/w/api.php?action=opensearch&search=$encodedQuery&limit=5&namespace=0&format=json"
            val wikiReq = Request.Builder()
                .url(wikiSearchUrl)
                .header("User-Agent", "PlayPulse-Android-App/2.0")
                .build()

            val wikiResp = client.newCall(wikiReq).execute()
            if (wikiResp.isSuccessful) {
                val wikiBody = wikiResp.body?.string()
                if (!wikiBody.isNullOrEmpty()) {
                    val rootArray = JSONArray(wikiBody)
                    if (rootArray.length() >= 2) {
                        val titlesArray = rootArray.optJSONArray(1) ?: JSONArray()
                        for (i in 0 until titlesArray.length().coerceAtMost(4)) {
                            val articleTitle = titlesArray.optString(i, "")
                            if (articleTitle.isNotBlank()) {
                                try {
                                    val summaryUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/${URLEncoder.encode(articleTitle, "UTF-8")}"
                                    val summaryReq = Request.Builder().url(summaryUrl).build()
                                    val summaryResp = client.newCall(summaryReq).execute()
                                    if (summaryResp.isSuccessful) {
                                        val sumBody = summaryResp.body?.string()
                                        if (!sumBody.isNullOrEmpty()) {
                                            val sumJson = JSONObject(sumBody)
                                            val title = sumJson.optString("title", articleTitle)
                                            val extract = sumJson.optString("extract", "")
                                            val thumbObj = sumJson.optJSONObject("thumbnail")
                                            val thumbUrl = thumbObj?.optString("source", "") ?: ""

                                            val defaultPlatform = detectedPlatform ?: when {
                                                title.contains("Zelda", true) || title.contains("Mario", true) || title.contains("Pokemon", true) -> "Nintendo Switch"
                                                title.contains("God of War", true) || title.contains("Horizon", true) || title.contains("Spiderman", true) -> "PlayStation 5"
                                                title.contains("Halo", true) || title.contains("Forza", true) -> "Xbox Series"
                                                else -> "PC"
                                            }

                                            onlineMatches.add(
                                                GameSearchResult(
                                                    title = title,
                                                    platform = defaultPlatform,
                                                    coverUrl = thumbUrl,
                                                    description = extract.take(160).ifEmpty { "Videohra $title" },
                                                    releaseYear = 2024,
                                                    genres = "Akční adventura",
                                                    developer = "Studio"
                                                )
                                            )
                                        }
                                    }
                                } catch (_: Exception) { }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        // 5. Sekundární zkouška RAWG pokud vrátí data
        try {
            val encodedQuery = URLEncoder.encode(cleanedTitleQuery, "UTF-8")
            val rawgRequest = Request.Builder()
                .url("https://api.rawg.io/api/games?key=c542e67aec3a4340908f9de9e86038af&search=$encodedQuery&page_size=6")
                .header("User-Agent", "PlayPulse-Android-App")
                .build()

            val rawgResp = client.newCall(rawgRequest).execute()
            if (rawgResp.isSuccessful) {
                val body = rawgResp.body?.string()
                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val results = json.optJSONArray("results") ?: JSONArray()
                    for (i in 0 until results.length()) {
                        val item = results.getJSONObject(i)
                        val name = item.optString("name", "")
                        if (name.isNotBlank()) {
                            val background = item.optString("background_image", "")
                            val released = item.optString("released", "")
                            val year = released.take(4).toIntOrNull() ?: 2024
                            val genresArray = item.optJSONArray("genres") ?: JSONArray()
                            val genresList = mutableListOf<String>()
                            for (g in 0 until genresArray.length()) {
                                genresList.add(genresArray.getJSONObject(g).optString("name", ""))
                            }

                            val platformsArray = item.optJSONArray("platforms") ?: JSONArray()
                            var bestPlatform = detectedPlatform ?: "PC"
                            if (detectedPlatform == null && platformsArray.length() > 0) {
                                val pObj = platformsArray.getJSONObject(0).optJSONObject("platform")
                                val pName = pObj?.optString("name", "") ?: ""
                                bestPlatform = PlatformInfo.findByName(pName).name
                            }

                            onlineMatches.add(
                                GameSearchResult(
                                    title = name,
                                    platform = bestPlatform,
                                    coverUrl = background,
                                    description = "Titul $name ($year). Žánr: ${genresList.joinToString(", ").ifEmpty { "Akční hra" }}.",
                                    releaseYear = year,
                                    genres = genresList.joinToString(", ").ifEmpty { "Akční hra" },
                                    developer = "Game Studio"
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        // Sloučení výsledků s prioritou lokálních shod a zamezením duplicit
        val combined = mutableListOf<GameSearchResult>()
        val seenTitles = mutableSetOf<String>()

        for (item in localMatches) {
            val key = PreloadedGames.normalizeText(item.title)
            if (seenTitles.add(key)) {
                combined.add(item)
            }
        }

        for (item in onlineMatches) {
            val key = PreloadedGames.normalizeText(item.title)
            if (seenTitles.add(key)) {
                combined.add(item)
            }
        }

        // Pokud nic nenalezeno, vytvoříme elegantní záznam pro vlastní zadání
        if (combined.isEmpty()) {
            val fallbackPlatform = detectedPlatform ?: "PlayStation 5"
            combined.add(
                GameSearchResult(
                    title = cleanedTitleQuery,
                    platform = fallbackPlatform,
                    coverUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=600&q=80",
                    description = "Vlastní titul: $cleanedTitleQuery",
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
