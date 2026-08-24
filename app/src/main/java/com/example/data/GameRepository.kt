package com.example.data

import com.example.model.GameEntity
import com.example.model.GameStatus
import com.example.model.PlaySessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GameRepository(
    private val gameDao: GameDao,
    private val playSessionDao: PlaySessionDao
) {
    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()
    val allSessions: Flow<List<PlaySessionEntity>> = playSessionDao.getAllSessions()
    val totalLibraryPlayTime: Flow<Long?> = gameDao.getTotalLibraryPlayTime()

    fun getGameById(id: Long): Flow<GameEntity?> = gameDao.getGameById(id)

    suspend fun getGameByIdDirect(id: Long): GameEntity? = withContext(Dispatchers.IO) {
        gameDao.getGameByIdDirect(id)
    }

    fun getSessionsForGame(gameId: Long): Flow<List<PlaySessionEntity>> =
        playSessionDao.getSessionsForGame(gameId)

    suspend fun insertGame(game: GameEntity): Long = withContext(Dispatchers.IO) {
        gameDao.insertGame(game)
    }

    suspend fun updateGame(game: GameEntity) = withContext(Dispatchers.IO) {
        gameDao.updateGame(game)
    }

    suspend fun deleteGame(id: Long) = withContext(Dispatchers.IO) {
        gameDao.deleteGameById(id)
    }

    suspend fun updateRatingAndReview(id: Long, rating: Int?, review: String, notes: String) =
        withContext(Dispatchers.IO) {
            gameDao.updateRatingAndReview(id, rating, review, notes)
        }

    suspend fun updateStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        gameDao.updateStatus(id, status)
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        gameDao.updateFavorite(id, isFavorite)
    }

    suspend fun addPlayTime(gameId: Long, durationSeconds: Long, timestamp: Long = System.currentTimeMillis()) =
        withContext(Dispatchers.IO) {
            gameDao.addPlayTime(gameId, durationSeconds, timestamp)
        }

    suspend fun recordSession(
        gameId: Long,
        durationSeconds: Long,
        startTime: Long,
        endTime: Long = System.currentTimeMillis(),
        sessionNotes: String = ""
    ) = withContext(Dispatchers.IO) {
        val game = gameDao.getGameByIdDirect(gameId)
        if (game != null) {
            // 1. Insert session record
            val session = PlaySessionEntity(
                gameId = gameId,
                gameTitle = game.title,
                platform = game.platform,
                startTime = startTime,
                endTime = endTime,
                durationSeconds = durationSeconds,
                notes = sessionNotes
            )
            playSessionDao.insertSession(session)

            // 2. Increment total play time and update last played
            gameDao.addPlayTime(gameId, durationSeconds, endTime)
        }
    }

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val count = gameDao.getGamesCount()
        if (count == 0) {
            // Seed a few beloved games to give immediate value and showcase features
            val sample1 = GameEntity(
                title = "The Legend of Zelda: Breath of the Wild",
                platform = "Nintendo Switch",
                coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3p2d.png",
                description = "Otevřený svět Hyrule, kde se probouzíte po 100 letech spánku. Prozkoumávejte svobodně krajinu, řešte hádanky ve svatyních a porazte Calamity Ganona.",
                releaseYear = 2017,
                genres = "Akční RPG, Adventura",
                developer = "Nintendo EPD",
                status = GameStatus.PLAYING.id,
                rating = 10,
                review = "Absolutní mistrovské dílo herního designu. Pocit objevování a svobody nemá v herním průmyslu obdoby.",
                notes = "Aktuálně mám 82 svatyní a 2 Divine Beasts hotové. Hledám Master Sword v Korok Forest.",
                totalPlayTimeSeconds = 148200L, // ~41h 10m
                lastPlayedTimestamp = System.currentTimeMillis() - 3600000L * 4,
                isFavorite = true
            )
            val id1 = gameDao.insertGame(sample1)
            playSessionDao.insertSession(
                PlaySessionEntity(
                    gameId = id1,
                    gameTitle = sample1.title,
                    platform = sample1.platform,
                    startTime = System.currentTimeMillis() - 3600000L * 6,
                    endTime = System.currentTimeMillis() - 3600000L * 4,
                    durationSeconds = 7200L,
                    notes = "Vyčištění Vah Ruta a průzkum Zora's Domain."
                )
            )

            val sample2 = GameEntity(
                title = "Elden Ring",
                platform = "PlayStation 5",
                coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.png",
                description = "Vstaňte, Poskvrnění, a nechte se vést milostí, abyste ovládli moc Prstenu Elden a stali se Elden Lordem v Mezizemí.",
                releaseYear = 2022,
                genres = "Souls-like, Akční RPG",
                developer = "FromSoftware",
                status = GameStatus.COMPLETED.id,
                rating = 10,
                review = "FromSoftware posunulo souls žánr na novou úroveň. Neuvěřitelná atmosféra, bossové a obrovská rozmanitost buildů.",
                notes = "Dohráno s Bleed Dex/Arcane buildem (Rivers of Blood). Malenia poražena na 35. pokus!",
                totalPlayTimeSeconds = 345600L, // ~96h
                lastPlayedTimestamp = System.currentTimeMillis() - 86400000L * 3,
                isFavorite = true
            )
            val id2 = gameDao.insertGame(sample2)

            val sample3 = GameEntity(
                title = "Chrono Trigger",
                platform = "Super Nintendo (SNES)",
                coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3nwm.png",
                description = "Jedno z nejlepších RPG všech dob s cestováním v čase, nezapomenutelnou hudbou Yasunori Mitsudy a vizuálem Akiry Toriyamy.",
                releaseYear = 1995,
                genres = "Retro JRPG",
                developer = "Square",
                status = GameStatus.PLAYING.id,
                rating = 9,
                review = "Nadčasová klasika. Žádné náhodné souboje, skvělý příběh s vícero konci a fenomenální soundtrack.",
                notes = "Rok 600 A.D. s Frogem v partě.",
                totalPlayTimeSeconds = 43200L, // ~12h
                lastPlayedTimestamp = System.currentTimeMillis() - 86400000L * 1,
                isFavorite = true
            )
            gameDao.insertGame(sample3)

            val sample4 = GameEntity(
                title = "Super Mario Odyssey",
                platform = "Nintendo Switch",
                coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1mxf.png",
                description = "Připojte se k Mariovi a jeho novému společníkovi Cappymu na masivní 3D dobrodružství napříč světy a sbírejte Měsíce síly.",
                releaseYear = 2017,
                genres = "3D Plošinovka",
                developer = "Nintendo EPD",
                status = GameStatus.COMPLETED.id,
                rating = 9,
                review = "Neskutečně zábavná hratelnost s převtělováním přes Cappyho. Metro Kingdom je legendární.",
                notes = "Sesbíráno 650 měsíců. Chybí Dark Side of the Moon.",
                totalPlayTimeSeconds = 108000L, // 30h
                lastPlayedTimestamp = System.currentTimeMillis() - 86400000L * 7,
                isFavorite = false
            )
            gameDao.insertGame(sample4)
        }
    }
}
