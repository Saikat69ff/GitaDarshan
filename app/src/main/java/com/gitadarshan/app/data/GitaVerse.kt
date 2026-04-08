package com.gitadarshan.app.data

data class GitaVerse(
    val chapter: Int,
    val verseNumber: Int,
    val verseId: String,       // e.g. "2.47"
    val english: String,
    val bengali: String
)

data class GitaChapter(
    val number: Int,
    val name: String,
    val nameBengali: String,
    val verses: List<GitaVerse>
)

data class GitaData(
    val chapters: List<GitaChapter>
)
