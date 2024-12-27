package com.ml.shubham0204.docqa.domain.splitters

import kotlin.math.max
import kotlin.math.min

// Provides a utility function for dividing a given text document into smaller,
// manageable chunks with the option to include overlaps between the chunks.

class WhiteSpaceSplitter {
    companion object {
        fun createChunks(
            docText: String,
            chunkSize: Int,
            separatorParagraph: String = "\n\n",
            separatorSentence: String = ".",
            separator: String = " ",
        ): List<String> {
            val textChunks = ArrayList<String>()
            docText.split(separatorParagraph).forEach { paragraph ->
                val sentences = paragraph.split(separatorSentence).map { it.trim() }
                sentences.forEach { sentence ->
                    var currChunk = ""
                    val chunks = ArrayList<String>()
                    sentence.split(separator).forEach { word ->
                        val newChunk = currChunk + (if (currChunk.isNotEmpty()) " " else "") + word
                        if (newChunk.length <= chunkSize) {
                            currChunk = newChunk
                        } else {
                            if (currChunk.isNotEmpty()) {
                                chunks.add(currChunk)
                            }
                            currChunk = word
                        }
                    }
                    if (currChunk.isNotEmpty()) {
                        chunks.add(currChunk)
                    }

                    // Add sentence-level chunks to the main list
                    textChunks.addAll(chunks)
                }
            }
            return textChunks
        }
    }
}