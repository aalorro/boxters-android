package com.artmondo.boxters.data.repository

import android.content.Context
import com.artmondo.boxters.domain.dictionary.Dictionary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object DictionaryRepository {

    var commonWords: Set<String> = emptySet()
        private set

    var commonWordsList: List<String> = emptyList()
        private set

    suspend fun loadDictionary(context: Context): Dictionary = withContext(Dispatchers.IO) {
        val dictionary = Dictionary()
        val words = mutableListOf<String>()

        context.assets.open("dictionary.txt").use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val word = line.trim().uppercase()
                    if (word.isNotEmpty()) {
                        words.add(word)
                    }
                    line = reader.readLine()
                }
            }
        }

        dictionary.load(words)

        // Load common words
        loadCommonWords(context)

        dictionary
    }

    private fun loadCommonWords(context: Context) {
        val words = mutableSetOf<String>()
        val wordsList = mutableListOf<String>()
        try {
            context.assets.open("common_words.txt").use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        val word = line.trim().uppercase()
                        if (word.isNotEmpty()) {
                            words.add(word)
                            wordsList.add(word)
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (_: Exception) { /* common_words.txt not found — use empty set */ }
        commonWords = words
        commonWordsList = wordsList
    }
}
