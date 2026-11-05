package com.example.data

import java.text.Normalizer

data class PreloadedGameData(
    val title: String,
    val platform: String,
    val coverUrl: String,
    val description: String,
    val releaseYear: Int,
    val genres: String,
    val developer: String
)

object PreloadedGames {
    val CATALOG = listOf(
        PreloadedGameData(
            title = "The Legend of Zelda: Tears of the Kingdom",
            platform = "Nintendo Switch",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co5vmg.png",
            description = "Epické dobrodružství v oblacích a hlubinách království Hyrule. Stavte unikátní vozidla a zbraně s novými schopnostmi Linka.",
            releaseYear = 2023,
            genres = "Akční adventura, Otevřený svět",
            developer = "Nintendo EPD"
        ),
        PreloadedGameData(
            title = "The Legend of Zelda: Breath of the Wild",
            platform = "Nintendo Switch",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3p2d.png",
            description = "Otevřený svět Hyrule, kde se probouzíte po 100 letech spánku. Prozkoumávejte svobodně krajinu, řešte hádanky ve svatyních a porazte Calamity Ganona.",
            releaseYear = 2017,
            genres = "Akční RPG, Adventura",
            developer = "Nintendo EPD"
        ),
        PreloadedGameData(
            title = "The Legend of Zelda: Ocarina of Time",
            platform = "Nintendo Switch",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1w69.png",
            description = "Revoluční 3D akční adventura s cestováním v čase, Z-targetingem a epickým příběhem chlapce bez víly v království Hyrule.",
            releaseYear = 1998,
            genres = "Akční adventura, Fantasy",
            developer = "Nintendo EAD"
        ),
        PreloadedGameData(
            title = "Super Mario Odyssey",
            platform = "Nintendo Switch",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1mxf.png",
            description = "Připojte se k Mariovi a jeho novému společníkovi Cappymu na masivní 3D dobrodružství napříč světy a sbírejte Měsíce síly.",
            releaseYear = 2017,
            genres = "3D Plošinovka",
            developer = "Nintendo EPD"
        ),
        PreloadedGameData(
            title = "Super Mario Bros. Wonder",
            platform = "Nintendo Switch",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co6q7f.png",
            description = "Nová 2D evoluce skákaček Mario s Wonder efekty, proměnami v slona a online kooperací v Květinovém království.",
            releaseYear = 2023,
            genres = "Plošinovka, Kooperace",
            developer = "Nintendo EPD"
        ),
        PreloadedGameData(
            title = "Metroid Prime Remastered",
            platform = "Nintendo Switch",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co627k.png",
            description = "Vylepšená legendární sci-fi střílečka z pohledu první osoby se Samus Aran na nepřátelské planetě Tallon IV.",
            releaseYear = 2023,
            genres = "Akční adventura, Sci-Fi",
            developer = "Retro Studios"
        ),
        PreloadedGameData(
            title = "Elden Ring",
            platform = "PlayStation 5",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.png",
            description = "Vstaňte, Poskvrnění, a nechte se vést milostí, abyste ovládli moc Prstenu Elden a stali se Elden Lordem v Mezizemí.",
            releaseYear = 2022,
            genres = "Souls-like, Akční RPG",
            developer = "FromSoftware"
        ),
        PreloadedGameData(
            title = "God of War Ragnarök",
            platform = "PlayStation 5",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co5s5v.png",
            description = "Kratos a Atreus se vydávají na mýtickou výpravu za odpověďmi před příchodem Ragnaröku přes všech Devět světů severské mytologie.",
            releaseYear = 2022,
            genres = "Akční adventura, Hack and slash",
            developer = "Santa Monica Studio"
        ),
        PreloadedGameData(
            title = "Cyberpunk 2077",
            platform = "PC",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co2mjs.png",
            description = "Otevřený akční RPG příběh zasazený do Night City, megalopole posedlé mocí, půvabem a tělesnými modifikacemi.",
            releaseYear = 2020,
            genres = "Akční RPG, Sci-Fi",
            developer = "CD PROJEKT RED"
        ),
        PreloadedGameData(
            title = "The Witcher 3: Wild Hunt",
            platform = "PC",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1wyy.png",
            description = "Jste Geralt z Rivie, nájemný lovec netvorů. Válkou zmítaným světem pátráte po Dítěti z proroctví — živé zbrani.",
            releaseYear = 2015,
            genres = "Akční RPG, Fantasy",
            developer = "CD PROJEKT RED"
        ),
        PreloadedGameData(
            title = "Baldur's Gate 3",
            platform = "PC",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co670h.png",
            description = "Hluboké taktické RPG nové generace zasazené do světa Dungeons & Dragons, kde vaše volby tvarují osud Forgotten Realms.",
            releaseYear = 2023,
            genres = "Taktické RPG, Fantasy",
            developer = "Larian Studios"
        ),
        PreloadedGameData(
            title = "Grand Theft Auto V",
            platform = "PlayStation 5",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co2lbd.png",
            description = "Tři různí zločinci riskují vše v sérii odvážných a nebezpečných loupeží v rozlehlém městě Los Santos.",
            releaseYear = 2013,
            genres = "Akční adventura, Otevřený svět",
            developer = "Rockstar North"
        ),
        PreloadedGameData(
            title = "Grand Theft Auto VI",
            platform = "PlayStation 5",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co7df4.png",
            description = "Nová kapitola série GTA zasazená do prosluněného a neony zalitého státu Leonida a města Vice City.",
            releaseYear = 2025,
            genres = "Akční adventura, Otevřený svět",
            developer = "Rockstar Games"
        ),
        PreloadedGameData(
            title = "Red Dead Redemption 2",
            platform = "PlayStation 5",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1q1f.png",
            description = "Amerika, 1899. Konec éry Divokého západu. Arthur Morgan a Van der Lindeův gang jsou psanci na útěku před federálními agenty.",
            releaseYear = 2018,
            genres = "Akční adventura, Western",
            developer = "Rockstar Games"
        ),
        PreloadedGameData(
            title = "Hollow Knight",
            platform = "PC",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co7d6a.png",
            description = "Vydejte se do hlubin zapomenutého království Hallownest. Nádherná ručně kreslená metroidvania s atmosférickým světem.",
            releaseYear = 2017,
            genres = "Metroidvania, Plošinovka",
            developer = "Team Cherry"
        ),
        PreloadedGameData(
            title = "Black Myth: Wukong",
            platform = "PlayStation 5",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co8j9x.png",
            description = "Akční RPG inspirované klasickým čínským mýtem Putování na západ v roli Vyvoleného opičího válečníka.",
            releaseYear = 2024,
            genres = "Akční RPG, Souls-like",
            developer = "Game Science"
        ),
        PreloadedGameData(
            title = "Forza Horizon 5",
            platform = "Xbox Series",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3ofx.png",
            description = "Prozkoumejte pestrý a neustále se vyvíjející otevřený svět Mexika s neomezenou řidičskou akcí ve stovkách nejlepších aut světa.",
            releaseYear = 2021,
            genres = "Závodní, Otevřený svět",
            developer = "Playground Games"
        ),
        PreloadedGameData(
            title = "Minecraft",
            platform = "PC",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co294o.png",
            description = "Kultovní sandbox hra o stavění, těžbě a přežívání v nekonečném procedurálně generovaném voxelovém světě.",
            releaseYear = 2011,
            genres = "Sandbox, Přežití",
            developer = "Mojang Studios"
        ),
        PreloadedGameData(
            title = "Final Fantasy VII Rebirth",
            platform = "PlayStation 5",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co72bl.png",
            description = "Cloud a jeho přátelé unikají z Midgaru a vydávají se napříč rozsáhlou planetou po stopách Sephirotha.",
            releaseYear = 2024,
            genres = "JRPG, Akční",
            developer = "Square Enix"
        ),
        PreloadedGameData(
            title = "The Last of Us Part I",
            platform = "PlayStation 5",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co5w3d.png",
            description = "Emoční cesta Joela a Ellie napříč zdevastovanou postpandemickou Amerikou plnou infikovaných i nebezpečných přeživších.",
            releaseYear = 2022,
            genres = "Akční adventura, Příběhová",
            developer = "Naughty Dog"
        ),
        PreloadedGameData(
            title = "Silent Hill 2",
            platform = "PlayStation 5",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co5r3z.png",
            description = "James Sunderland dostane dopis od své zesnulé manželky a vydává se do mlhou zahaleného městečka Silent Hill.",
            releaseYear = 2024,
            genres = "Survival horor, Psychologický",
            developer = "Bloober Team"
        ),
        PreloadedGameData(
            title = "Super Mario 64",
            platform = "Nintendo Switch",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x8a.png",
            description = "Průkopnická 3D plošinovka, která definovala ovládání třetí dimenze a kameru v zámku princezny Peach.",
            releaseYear = 1996,
            genres = "3D Plošinovka, Retro",
            developer = "Nintendo EAD"
        ),
        PreloadedGameData(
            title = "Chrono Trigger",
            platform = "PC",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3nwm.png",
            description = "Jedno z nejlepších RPG všech dob s cestováním v čase, nezapomenutelnou hudbou a vizuálem Akiry Toriyamy.",
            releaseYear = 1995,
            genres = "Retro JRPG, Fantasy",
            developer = "Square"
        ),
        PreloadedGameData(
            title = "Castlevania: Symphony of the Night",
            platform = "Ostatní",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1tf1.png",
            description = "Alucard, syn hraběte Draculy, se probouzí a prozkoumává hrad plný monster a tajemství v definující hře Metroidvania.",
            releaseYear = 1997,
            genres = "Metroidvania, Akční RPG",
            developer = "Konami"
        ),
        PreloadedGameData(
            title = "Half-Life 2",
            platform = "PC",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1w9k.png",
            description = "Gordon Freeman se chopí páčidla a gravitační zbraně v dystopickém City 17 pod nadvládou mimozemského impéria Combine.",
            releaseYear = 2004,
            genres = "FPS, Sci-Fi",
            developer = "Valve"
        ),
        PreloadedGameData(
            title = "Portal 2",
            platform = "PC",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1rs4.png",
            description = "Inovativní logická hra z pohledu první osoby s portálovou zbraní, GLaDOS, Wheatleym a skvělým kooperativním režimem.",
            releaseYear = 2011,
            genres = "Logická, Sci-Fi",
            developer = "Valve"
        )
    )

    // Odstraní diakritiku, speciální znaky a převede na malá písmena
    fun normalizeText(input: String): String {
        val decomposed = Normalizer.normalize(input.lowercase(), Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .trim()
    }

    // Odstraní běžná doplňková slova (pro, na, for, switch 2, ps5...)
    fun cleanSearchTokens(rawQuery: String): List<String> {
        val stopWords = setOf("pro", "na", "for", "on", "the", "of", "and", "hra", "game", "switch", "switch2", "ps5", "xbox", "pc")
        return normalizeText(rawQuery)
            .split(Regex("\\s+"))
            .filter { it.length >= 2 && !stopWords.contains(it) }
    }

    fun search(query: String): List<PreloadedGameData> {
        val rawTrimmed = query.trim()
        if (rawTrimmed.isEmpty()) return CATALOG.take(12)

        val normQuery = normalizeText(rawTrimmed)
        val tokens = cleanSearchTokens(rawTrimmed)

        // 1. Přesná shoda nebo podřetězec bez ohledu na velikost písmen a diakritiku
        val directMatches = CATALOG.filter { game ->
            val normTitle = normalizeText(game.title)
            val normDev = normalizeText(game.developer)
            val normGenres = normalizeText(game.genres)
            normTitle.contains(normQuery) || normDev.contains(normQuery) || normGenres.contains(normQuery)
        }
        if (directMatches.isNotEmpty()) return directMatches

        // 2. Tokenové vyhledávání: najde hru pokud obsahuje hledaná klíčová slova
        // (např. "ocarina of time pro switch 2" -> obsahuje "ocarina" a "time")
        if (tokens.isNotEmpty()) {
            val tokenMatches = CATALOG.filter { game ->
                val normTitle = normalizeText(game.title)
                // Hra musí obsahovat alespoň hlavní tokeny
                tokens.all { token -> normTitle.contains(token) } ||
                tokens.count { token -> normTitle.contains(token) } >= (tokens.size.coerceAtLeast(2) / 2)
            }
            if (tokenMatches.isNotEmpty()) return tokenMatches
        }

        // 3. Volnější shoda na libovolný token
        return CATALOG.filter { game ->
            val normTitle = normalizeText(game.title)
            tokens.any { token -> token.length >= 3 && normTitle.contains(token) }
        }
    }
}
