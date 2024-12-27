package com.ml.shubham0204.docqa.domain.llm

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.content
import com.ml.shubham0204.docqa.BuildConfig
import com.ml.shubham0204.docqa.data.APIDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
class GeminiRemoteAPI(private val context: Context) {

    private val apiKey: String?
    private var generativeModel: GenerativeModel? = null

    init {
        // Fetch API key from DataStore
        val apiKeyDataStore = APIDataStore(context)
        apiKey = runBlocking {
            apiKeyDataStore.apiKey.firstOrNull()
        }

        apiKey?.let {
            // Configure generation parameters
            val configBuilder = GenerationConfig.Builder()
            configBuilder.topP = 0.4f
            configBuilder.temperature = 0.3f

            generativeModel = GenerativeModel(modelName = "gemini-1.5-flash",
                apiKey = apiKey,
                generationConfig = configBuilder.build(),
                systemInstruction = content {
                    text("You are an intelligent search engine. You will be provided with some retrieved context, as well as the user's query. Your job is to understand the request and answer based on the retrieved context.")
                })
        }
    }

    suspend fun getResponse(prompt: String): String? = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "API key is missing. Please set it first",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.e("APP", "API Key is missing. Cannot generate response.")
            return@withContext null
        }
        Log.e("APP", "Prompt given: $prompt")
        val response = generativeModel?.generateContent(prompt)
        return@withContext response?.text
    }
}