package com.artmondo.boxters.domain.dictionary

class TrieNode {
    val children = arrayOfNulls<TrieNode>(26)
    var isEndOfWord = false
}

class Dictionary {
    private val root = TrieNode()
    var wordCount = 0
        private set
    var loaded = false
        private set

    private val wordList = mutableListOf<String>()

    fun load(words: List<String>) {
        for (word in words) {
            insert(word.uppercase())
        }
        loaded = true
    }

    private fun insert(word: String) {
        var node = root
        for (ch in word) {
            val idx = ch - 'A'
            if (idx < 0 || idx >= 26) return
            if (node.children[idx] == null) {
                node.children[idx] = TrieNode()
            }
            node = node.children[idx]!!
        }
        if (!node.isEndOfWord) {
            node.isEndOfWord = true
            wordCount++
            wordList.add(word)
        }
    }

    fun isWord(word: String): Boolean {
        val node = traverse(word.uppercase()) ?: return false
        return node.isEndOfWord
    }

    fun isPrefix(prefix: String): Boolean {
        return traverse(prefix.uppercase()) != null
    }

    private fun traverse(str: String): TrieNode? {
        var node = root
        for (ch in str) {
            val idx = ch - 'A'
            if (idx < 0 || idx >= 26) return null
            node = node.children[idx] ?: return null
        }
        return node
    }

    fun getWordList(): List<String> = wordList
}
