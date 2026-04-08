package com.gitadarshan.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

class GitaRepository(private val context: Context) {

    private var allVerses: List<GitaVerse> = emptyList()

    init {
        loadVerses()
    }

    private fun loadVerses() {
        try {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier("gita_verses", "raw", context.packageName)
            )
            val json = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val root = gson.fromJson(json, JsonObject::class.java)
            val chaptersArray = root.getAsJsonArray("chapters")

            val verses = mutableListOf<GitaVerse>()
            for (chapterEl in chaptersArray) {
                val chObj = chapterEl.asJsonObject
                val chNum = chObj.get("number").asInt
                val versesArray = chObj.getAsJsonArray("verses")
                for (verseEl in versesArray) {
                    val v = verseEl.asJsonObject
                    verses.add(
                        GitaVerse(
                            chapter = chNum,
                            verseNumber = v.get("verse_number").asInt,
                            verseId = v.get("verse_id").asString,
                            english = v.get("english").asString,
                            bengali = v.get("bengali").asString
                        )
                    )
                }
            }
            allVerses = verses
        } catch (e: Exception) {
            e.printStackTrace()
            allVerses = fallbackVerses()
        }
    }

    fun getRandomVerse(): GitaVerse {
        if (allVerses.isEmpty()) return fallbackVerses().random()
        return allVerses.random()
    }

    fun getVerseCount(): Int = allVerses.size

    private fun fallbackVerses(): List<GitaVerse> = listOf(
        GitaVerse(2, 47, "2.47",
            "You have the right to perform your actions, but never to the fruits of your actions. Never let the fruits of action be your motive, nor let your attachment be to inaction.",
            "কর্মে তোমার অধিকার আছে, কিন্তু কর্মফলে নয়। কর্মফল যেন তোমার কর্মের উদ্দেশ্য না হয়, আবার অকর্মণ্যতায়ও আসক্ত হয়ো না।"),
        GitaVerse(18, 66, "18.66",
            "Abandon all varieties of religion and just surrender unto Me. I shall deliver you from all sinful reactions. Do not fear.",
            "সমস্ত ধর্ম পরিত্যাগ করে শুধু আমার শরণ নাও। আমি তোমাকে সমস্ত পাপ থেকে মুক্ত করব। ভয় পেয়ো না।")
    )
}
