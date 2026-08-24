package com.example.data

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
            title = "The Legend of Zelda: Breath of the Wild",
            platform = "Nintendo Switch",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3p2d.png",
            description = "Otevřený svět Hyrule, kde se probouzíte po 100 letech spánku. Prozkoumávejte svobodně krajinu, řešte hádanky ve svatyních a porazte Calamity Ganona.",
            releaseYear = 2017,
            genres = "Akční RPG, Adventura",
            developer = "Nintendo EPD"
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
            platform = "PC (Steam / GOG / Epic)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co2mjs.png",
            description = "Otevřený akční RPG příběh zasazený do Night City, megalopole posedlé mocí, půvabem a tělesnými modifikacemi.",
            releaseYear = 2020,
            genres = "Akční RPG, Sci-Fi",
            developer = "CD PROJEKT RED"
        ),
        PreloadedGameData(
            title = "The Witcher 3: Wild Hunt",
            platform = "PC (Steam / GOG / Epic)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1wyy.png",
            description = "Jste Geralt z Rivie, nájemný lovec netvorů. Válkou zmítaným světem pátráte po Dítěti z proroctví — živé zbrani, která dokáže změnit tvář světa.",
            releaseYear = 2015,
            genres = "Akční RPG, Fantasy",
            developer = "CD PROJEKT RED"
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
            title = "Red Dead Redemption 2",
            platform = "PlayStation 4",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1q1f.png",
            description = "Amerika, 1899. Konec éry Divokého západu. Arthur Morgan a Van der Lindeův gang jsou psanci na útěku před federálními agenty.",
            releaseYear = 2018,
            genres = "Akční adventura, Western",
            developer = "Rockstar Games"
        ),
        PreloadedGameData(
            title = "Hollow Knight",
            platform = "Steam Deck",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co7d6a.png",
            description = "Vydejte se do hlubin zapomenutého království Hallownest. Nádherná ručně kreslená metroidvania s atmosférickým světem a náročnými souboji.",
            releaseYear = 2017,
            genres = "Metroidvania, Plošinovka",
            developer = "Team Cherry"
        ),
        PreloadedGameData(
            title = "Baldur's Gate 3",
            platform = "PC (Steam / GOG / Epic)",
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
            title = "Persona 5 Royal",
            platform = "PlayStation 4",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1nic.png",
            description = "Nasaďte si masku Jokera a přidejte se k Phantom Thieves of Hearts. Měňte srdce zkažených lidí v Tokiu a žijte dvojí život studenta.",
            releaseYear = 2019,
            genres = "JRPG, Sociální simulátor",
            developer = "Atlus"
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
            title = "Bloodborne",
            platform = "PlayStation 4",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1r77.png",
            description = "Temné gotické město Yharnam zamořené strašlivou epidemií. Čelte svým obavám v roli Lovce v bezútěšné noci lovu.",
            releaseYear = 2015,
            genres = "Souls-like, Horor",
            developer = "FromSoftware"
        ),
        PreloadedGameData(
            title = "Halo 3",
            platform = "Xbox 360",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x7e.png",
            description = "Dokončete boj. Master Chief se vrací, aby zachránil lidstvo v epickém finále původní trilogie Halo.",
            releaseYear = 2007,
            genres = "FPS, Sci-Fi",
            developer = "Bungie"
        ),
        PreloadedGameData(
            title = "Super Mario World",
            platform = "Super Nintendo (SNES)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x8b.png",
            description = "Legendární 16-bitová plošinovka představující dinosaura Yoshiho a kouzelný Dinosaur Land plný tajných cest a úrovní.",
            releaseYear = 1990,
            genres = "Retro plošinovka",
            developer = "Nintendo EAD"
        ),
        PreloadedGameData(
            title = "Chrono Trigger",
            platform = "Super Nintendo (SNES)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3nwm.png",
            description = "Jedno z nejlepších RPG všech dob s cestováním v čase, nezapomenutelnou hudbou Yasunori Mitsudy a vizuálem Akiry Toriyamy.",
            releaseYear = 1995,
            genres = "Retro JRPG",
            developer = "Square"
        ),
        PreloadedGameData(
            title = "Metal Gear Solid",
            platform = "PlayStation 1 (PSX)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1tc1.png",
            description = "Solid Snake infiltruje zařízení na likvidaci jaderných zbraní na ostrově Shadow Moses. Kinematografická stealth revoluce Hidea Kojimy.",
            releaseYear = 1998,
            genres = "Stealth akce",
            developer = "Konami"
        ),
        PreloadedGameData(
            title = "Grand Theft Auto: San Andreas",
            platform = "PlayStation 2",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co2kch.png",
            description = "CJ se vrací do Los Santos na počátku 90. let, aby zachránil svou rodinu a získal kontrolu nad ulicemi státu San Andreas.",
            releaseYear = 2004,
            genres = "Akční adventura, Open World",
            developer = "Rockstar North"
        ),
        PreloadedGameData(
            title = "Resident Evil 4",
            platform = "Nintendo GameCube",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1wzv.png",
            description = "Leon S. Kennedy je vyslán do odlehlé španělské vesnice zachránit unesenou dceru amerického prezidenta.",
            releaseYear = 2005,
            genres = "Survival horor, Akce",
            developer = "Capcom"
        ),
        PreloadedGameData(
            title = "Super Metroid",
            platform = "Super Nintendo (SNES)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1pba.png",
            description = "Samus Aran se vrací na planetu Zebes, aby získala ukradené mládě Metroida ze spárů vesmírných pirátů a Ridleyho.",
            releaseYear = 1994,
            genres = "Metroidvania, Sci-Fi",
            developer = "Nintendo R&D1"
        ),
        PreloadedGameData(
            title = "Super Mario Bros. 3",
            platform = "Nintendo Entertainment System (NES)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x8c.png",
            description = "Jedna z nejuznávanějších her všech dob s mapou světa, novými obleky jako Tanooki a žabí oblek.",
            releaseYear = 1988,
            genres = "Retro plošinovka",
            developer = "Nintendo R&D4"
        ),
        PreloadedGameData(
            title = "The Legend of Zelda: Ocarina of Time",
            platform = "Nintendo 64",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1w69.png",
            description = "Revoluční 3D akční adventura s cestováním v čase, Z-targetingem a epickým příběhem chlapce bez víly v království Hyrule.",
            releaseYear = 1998,
            genres = "Akční adventura, Fantasy",
            developer = "Nintendo EAD"
        ),
        PreloadedGameData(
            title = "Castlevania: Symphony of the Night",
            platform = "PlayStation 1 (PSX)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1tf1.png",
            description = "Alucard, syn hraběte Draculy, se probouzí a prozkoumává hrad plný monster a tajemství v definující hře žánru Metroidvania.",
            releaseYear = 1997,
            genres = "Metroidvania, Akční RPG",
            developer = "Konami"
        ),
        PreloadedGameData(
            title = "Pokémon Emerald",
            platform = "Game Boy Advance",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1rc4.png",
            description = "Prozkoumejte region Hoenn, zastavte konflikt mezi Team Magma a Team Aqua a probuďte legendárního Rayquazu.",
            releaseYear = 2004,
            genres = "JRPG, Sběratelské",
            developer = "Game Freak"
        ),
        PreloadedGameData(
            title = "Forza Horizon 5",
            platform = "Xbox Series X|S",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3ofx.png",
            description = "Prozkoumejte pestrý a neustále se vyvíjející otevřený svět Mexika s neomezenou řidičskou akcí ve stovkách nejlepších aut světa.",
            releaseYear = 2021,
            genres = "Závodní, Otevřený svět",
            developer = "Playground Games"
        ),
        PreloadedGameData(
            title = "Minecraft",
            platform = "PC (Steam / GOG / Epic)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co294o.png",
            description = "Kultovní sandbox hra o stavění, těžbě a přežívání v nekonečném procedurálně generovaném voxelovém světě.",
            releaseYear = 2011,
            genres = "Sandbox, Přežití",
            developer = "Mojang Studios"
        ),
        PreloadedGameData(
            title = "Half-Life 2",
            platform = "PC (Steam / GOG / Epic)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1w9k.png",
            description = "Gordon Freeman se chopí páčidla a gravitační zbraně v dystopickém City 17 pod nadvládou mimozemského impéria Combine.",
            releaseYear = 2004,
            genres = "FPS, Sci-Fi",
            developer = "Valve"
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
            title = "Dark Souls",
            platform = "PlayStation 3",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1r3k.png",
            description = "Vydejte se do temného fantasy království Lordran. Neodpouštějící obtížnost, hluboký lore a nezapomenutelné souboje s bossy.",
            releaseYear = 2011,
            genres = "Akční RPG, Souls-like",
            developer = "FromSoftware"
        ),
        PreloadedGameData(
            title = "Portal 2",
            platform = "PC (Steam / GOG / Epic)",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1rs4.png",
            description = "Inovativní logická hra z pohledu první osoby s portálovou zbraní, GLaDOS, Wheatleym a skvělým kooperativním režimem.",
            releaseYear = 2011,
            genres = "Logická, Sci-Fi",
            developer = "Valve"
        )
    )

    fun search(query: String): List<PreloadedGameData> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return CATALOG.take(10)
        return CATALOG.filter {
            it.title.contains(trimmed, ignoreCase = true) ||
            it.genres.contains(trimmed, ignoreCase = true) ||
            it.developer.contains(trimmed, ignoreCase = true) ||
            it.platform.contains(trimmed, ignoreCase = true)
        }
    }
}
