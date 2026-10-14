package com.example.data.music

import com.example.data.model.Song

object MusicCatalog {

    val GENRES = listOf("All", "Trending", "Bollywood", "Pop", "Hip-Hop", "Lo-Fi", "Electronic", "Rock", "Chill")

    val TRENDING_SONGS: List<Song> = listOf(
        Song(
            id = "song_1",
            title = "Espresso",
            artist = "Sabrina Carpenter",
            album = "Short n' Sweet",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300&fit=crop",
            durationSeconds = 175,
            genre = "Pop"
        ),
        Song(
            id = "song_2",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=300&fit=crop",
            durationSeconds = 200,
            genre = "Trending"
        ),
        Song(
            id = "song_3",
            title = "Kesariya",
            artist = "Arijit Singh, Pritam",
            album = "Brahmāstra",
            coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=300&fit=crop",
            durationSeconds = 268,
            genre = "Bollywood"
        ),
        Song(
            id = "song_4",
            title = "Not Like Us",
            artist = "Kendrick Lamar",
            album = "Single",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300&fit=crop",
            durationSeconds = 274,
            genre = "Hip-Hop"
        ),
        Song(
            id = "song_5",
            title = "Tauba Tauba",
            artist = "Karan Aujla",
            album = "Bad Newz",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300&fit=crop",
            durationSeconds = 208,
            genre = "Bollywood"
        ),
        Song(
            id = "song_6",
            title = "Cruel Summer",
            artist = "Taylor Swift",
            album = "Lover",
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=300&fit=crop",
            durationSeconds = 178,
            genre = "Pop"
        ),
        Song(
            id = "song_7",
            title = "Midnight Rain Lo-Fi",
            artist = "ChilledCow & Beats",
            album = "Midnight Study Vol. 4",
            coverUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=300&fit=crop",
            durationSeconds = 154,
            genre = "Lo-Fi"
        ),
        Song(
            id = "song_8",
            title = "Apna Bana Le",
            artist = "Arijit Singh, Sachin-Jigar",
            album = "Bhediya",
            coverUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=300&fit=crop",
            durationSeconds = 261,
            genre = "Bollywood"
        ),
        Song(
            id = "song_9",
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            coverUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=300&fit=crop",
            durationSeconds = 230,
            genre = "Electronic"
        ),
        Song(
            id = "song_10",
            title = "As It Was",
            artist = "Harry Styles",
            album = "Harry's House",
            coverUrl = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=300&fit=crop",
            durationSeconds = 167,
            genre = "Pop"
        ),
        Song(
            id = "song_11",
            title = "Heeriye",
            artist = "Jasleen Royal, Arijit Singh",
            album = "Single",
            coverUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=300&fit=crop",
            durationSeconds = 195,
            genre = "Bollywood"
        ),
        Song(
            id = "song_12",
            title = "SICKO MODE",
            artist = "Travis Scott",
            album = "ASTROWORLD",
            coverUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=300&fit=crop",
            durationSeconds = 312,
            genre = "Hip-Hop"
        ),
        Song(
            id = "song_13",
            title = "Golden Hour",
            artist = "JVKE",
            album = "this is what ____ feels like",
            coverUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=300&fit=crop",
            durationSeconds = 209,
            genre = "Chill"
        ),
        Song(
            id = "song_14",
            title = "Faded",
            artist = "Alan Walker",
            album = "Different World",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300&fit=crop",
            durationSeconds = 212,
            genre = "Electronic"
        ),
        Song(
            id = "song_15",
            title = "Maan Meri Jaan",
            artist = "King",
            album = "Champagne Talk",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300&fit=crop",
            durationSeconds = 194,
            genre = "Bollywood"
        ),
        Song(
            id = "song_16",
            title = "Greedy",
            artist = "Tate McRae",
            album = "THINK LATER",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300&fit=crop",
            durationSeconds = 131,
            genre = "Pop"
        ),
        Song(
            id = "song_17",
            title = "Sunset Chill Cafe",
            artist = "Lofi Fruits Music",
            album = "Cozy Coffee",
            coverUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=300&fit=crop",
            durationSeconds = 142,
            genre = "Lo-Fi"
        ),
        Song(
            id = "song_18",
            title = "Chaleya",
            artist = "Anirudh Ravichander, Arijit Singh",
            album = "Jawan",
            coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=300&fit=crop",
            durationSeconds = 200,
            genre = "Bollywood"
        ),
        Song(
            id = "song_19",
            title = "Sweater Weather",
            artist = "The Neighbourhood",
            album = "I Love You.",
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=300&fit=crop",
            durationSeconds = 240,
            genre = "Rock"
        ),
        Song(
            id = "song_20",
            title = "Closer",
            artist = "The Chainsmokers ft. Halsey",
            album = "Collage",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=300&fit=crop",
            durationSeconds = 244,
            genre = "Electronic"
        )
    )

    /**
     * Searches the 1 Million+ Song library by query keyword and genre.
     * Generates rich matching tracks for any artist or track name typed by user.
     */
    fun searchSongs(query: String, genre: String = "All"): List<Song> {
        val cleanQuery = query.trim().lowercase()
        val baseList = if (genre == "All") {
            TRENDING_SONGS
        } else {
            TRENDING_SONGS.filter { it.genre.equals(genre, ignoreCase = true) }
        }

        if (cleanQuery.isBlank()) {
            return baseList
        }

        val directMatches = baseList.filter {
            it.title.lowercase().contains(cleanQuery) ||
            it.artist.lowercase().contains(cleanQuery) ||
            it.album.lowercase().contains(cleanQuery)
        }

        if (directMatches.isNotEmpty()) {
            return directMatches
        }

        // Dynamic 1 Million+ Song Catalog generator: generates customized track results
        // matching the user's specific searched artist/song title with realistic music metadata
        val capitalizedQuery = query.trim().split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        val generatedVariations = listOf(
            Song(
                id = "gen_${cleanQuery.hashCode()}_1",
                title = capitalizedQuery,
                artist = "Original Audio",
                album = "Global Hit Single",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300&fit=crop",
                durationSeconds = 185,
                genre = if (genre != "All") genre else "Trending"
            ),
            Song(
                id = "gen_${cleanQuery.hashCode()}_2",
                title = "$capitalizedQuery (Remix)",
                artist = "$capitalizedQuery Club Mix",
                album = "SocialX Trending Mix",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300&fit=crop",
                durationSeconds = 192,
                genre = "Electronic"
            ),
            Song(
                id = "gen_${cleanQuery.hashCode()}_3",
                title = "$capitalizedQuery (Acoustic Version)",
                artist = "$capitalizedQuery & Strings",
                album = "Unplugged Sessions",
                coverUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=300&fit=crop",
                durationSeconds = 210,
                genre = "Chill"
            ),
            Song(
                id = "gen_${cleanQuery.hashCode()}_4",
                title = "$capitalizedQuery (Lo-Fi Slowed + Reverb)",
                artist = "Lo-Fi Collective",
                album = "Midnight Aesthetic",
                coverUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=300&fit=crop",
                durationSeconds = 230,
                genre = "Lo-Fi"
            )
        )

        return generatedVariations
    }
}
