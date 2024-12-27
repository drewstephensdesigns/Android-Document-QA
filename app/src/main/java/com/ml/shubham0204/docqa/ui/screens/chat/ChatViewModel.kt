package com.ml.shubham0204.docqa.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ml.shubham0204.docqa.data.APIDataStore
import com.ml.shubham0204.docqa.data.ChunksDB
import com.ml.shubham0204.docqa.data.DocumentsDB
import com.ml.shubham0204.docqa.data.RetrievedContext
import com.ml.shubham0204.docqa.domain.embeddings.SentenceEmbeddingProvider
import com.ml.shubham0204.docqa.domain.llm.GeminiRemoteAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatViewModel(
    private val documentsDB: DocumentsDB,
    private val chunksDB: ChunksDB,
    private val geminiRemoteAPI: GeminiRemoteAPI,
    private val sentenceEncoder: SentenceEmbeddingProvider,
    private val apiKeyDataStore: APIDataStore
) : ViewModel() {
    private val _questionState = MutableStateFlow("")
    val questionState: StateFlow<String> = _questionState

    private val _responseState = MutableStateFlow("")
    val responseState: StateFlow<String> = _responseState

    private val _isGeneratingResponseState = MutableStateFlow(false)
    val isGeneratingResponseState: StateFlow<Boolean> = _isGeneratingResponseState

    private val _retrievedContextListState = MutableStateFlow(emptyList<RetrievedContext>())
    val retrievedContextListState: StateFlow<List<RetrievedContext>> = _retrievedContextListState

    private val _isApiKeySet = MutableStateFlow(false)
    val isApiKeySet: StateFlow<Boolean> = _isApiKeySet

    init {
        viewModelScope.launch {
            val apiKey = apiKeyDataStore.apiKey.first()
            _isApiKeySet.value = !apiKey.isNullOrBlank()
        }
    }

    fun getAnswer(
        query: String,
        prompt: String,
    ) {
        if (!isApiKeySet.value){
            throw IllegalStateException("API Key is not set")
        }

        _isGeneratingResponseState.value = true
        _questionState.value = query
        var jointContext = ""
        val retrievedContextList = ArrayList<RetrievedContext>()
        val queryEmbedding = sentenceEncoder.encodeText(query)
        chunksDB.getSimilarChunks(queryEmbedding, n = 5).forEach {
            val cleanedChunk = it.second.chunkData
                .replace(Regex("\\s+"), " ")
                .trim()
            jointContext += " $cleanedChunk"
            retrievedContextList.add(RetrievedContext(it.second.docFileName, cleanedChunk))
        }
        val inputPrompt = prompt
            .replace("\$CONTEXT", jointContext.trim())
            .replace("\$QUERY", query.trim())
        CoroutineScope(Dispatchers.IO).launch {
            geminiRemoteAPI.getResponse(inputPrompt)?.let { llmResponse ->
                val cleanedResponse = llmResponse
                    .replace(Regex("\\s+"), " ") // Normalize spaces and newlines
                    .trim()
                _responseState.value = cleanedResponse
                _isGeneratingResponseState.value = false
                _retrievedContextListState.value = retrievedContextList
            }
        }
    }

    fun canGenerateAnswers(): Boolean = documentsDB.getDocsCount() > 0

    fun validateApiKey(): Boolean = _isApiKeySet.value
}
