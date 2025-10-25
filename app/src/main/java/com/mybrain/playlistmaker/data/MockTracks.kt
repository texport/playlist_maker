package com.mybrain.playlistmaker.data

import com.mybrain.playlistmaker.models.Track

object MockTracks {
    private val tracks = listOf(
        Track(
            trackId = 1L,
            trackName = "Smells Like Teen Spirit",
            artistName = "Nirvana",
            trackTime = "5:01",
            artworkUrl100 = "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg",
            collectionName = "Nevermind",
            releaseDate = "1991-09-10",
            primaryGenreName = "Rock",
            country = "USA"
        ),
        Track(
            trackId = 2L,
            trackName = "Billie Jean",
            artistName = "Michael Jackson",
            trackTime = "4:35",
            artworkUrl100 = "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg",
            collectionName = "Thriller",
            releaseDate = "1982-11-30",
            primaryGenreName = "Pop",
            country = "USA"
        ),
        Track(
            trackId = 3L,
            trackName = "Stayin' Alive",
            artistName = "Bee Gees",
            trackTime = "4:10",
            artworkUrl100 = "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg",
            collectionName = "Saturday Night Fever",
            releaseDate = "1977-11-15",
            primaryGenreName = "Disco",
            country = "UK"
        ),
        Track(
            trackId = 4L,
            trackName = "Whole Lotta Love",
            artistName = "Led Zeppelin",
            trackTime = "5:33",
            artworkUrl100 = "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg",
            collectionName = "Led Zeppelin II",
            releaseDate = "1969-10-22",
            primaryGenreName = "Hard Rock",
            country = "UK"
        ),
        Track(
            trackId = 5L,
            trackName = "Sweet Child O'Mine",
            artistName = "Guns N' Roses",
            trackTime = "5:03",
            artworkUrl100 = "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg",
            collectionName = "Appetite for Destruction",
            releaseDate = "1987-07-21",
            primaryGenreName = "Rock",
            country = "USA"
        ),
        Track(
            trackId = 6L,
            trackName = "Bohemian Rhapsody",
            artistName = "Queen",
            trackTime = "5:55",
            artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/67/30/8f/67308f2a-2cfa-cb6e-4f5c-54b6c86e13b7/00000602577554.rgb.jpg/100x100bb.jpg",
            collectionName = "A Night at the Opera",
            releaseDate = "1975-11-21",
            primaryGenreName = "Rock",
            country = "UK"
        ),
        Track(
            trackId = 7L,
            trackName = "Hotel California",
            artistName = "Eagles",
            trackTime = "6:30",
            artworkUrl100 = "https://is2-ssl.mzstatic.com/image/thumb/Music124/v4/19/40/f3/1940f3f2-06d4-6c9e-70cb-33a9af0b53d0/190295875595.jpg/100x100bb.jpg",
            collectionName = "Hotel California",
            releaseDate = "1976-12-08",
            primaryGenreName = "Rock",
            country = "USA"
        ),
        Track(
            trackId = 8L,
            trackName = "Imagine",
            artistName = "John Lennon",
            trackTime = "3:07",
            artworkUrl100 = "https://is4-ssl.mzstatic.com/image/thumb/Music125/v4/56/2c/9e/562c9eb1-1c9d-2468-cc83-529b6b3f96cc/00007777450036.rgb.jpg/100x100bb.jpg",
            collectionName = "Imagine",
            releaseDate = "1971-09-09",
            primaryGenreName = "Soft Rock",
            country = "UK"
        ),
        Track(
            trackId = 9L,
            trackName = "Shape of You",
            artistName = "Ed Sheeran",
            trackTime = "3:53",
            artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/6d/5e/34/6d5e345b-2e49-899b-ef68-20e23371f482/190295851580.jpg/100x100bb.jpg",
            collectionName = "÷ (Divide)",
            releaseDate = "2017-03-03",
            primaryGenreName = "Pop",
            country = "UK"
        ),
        Track(
            trackId = 10L,
            trackName = "Lose Yourself",
            artistName = "Eminem",
            trackTime = "5:26",
            artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/11/8b/f9/118bf98c-1f19-3c94-77e9-7cc205a75bc1/00606949355525.rgb.jpg/100x100bb.jpg",
            collectionName = "8 Mile (Soundtrack)",
            releaseDate = "2002-10-28",
            primaryGenreName = "Hip-Hop/Rap",
            country = "USA"
        )
    )

    fun getTracks(): List<Track> = tracks
}