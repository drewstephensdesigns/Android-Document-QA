package com.ml.shubham0204.docqa.ui.screens.editAPI

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ml.shubham0204.docqa.data.APIDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ApiKeyViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKeyDataStore = APIDataStore(application)
    val apiKey: Flow<String?> = apiKeyDataStore.apiKey

    private val _apiKeyHistory = MutableStateFlow<List<String>>(emptyList())

    // Merge apiKey and _apiKeyHistory flow to react to changes
    val apiKeyHistory: StateFlow<List<String>> = combine(apiKey, _apiKeyHistory) { currentApiKey, history ->
        val mutableHistory = history.toMutableList()
        if (currentApiKey != null && !mutableHistory.contains(currentApiKey)) {
            mutableHistory.add(0, currentApiKey) // Add to the beginning
        }
        mutableHistory.distinct().take(MAX_HISTORY_SIZE)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Max size of keys to display in list
    companion object {
        private const val MAX_HISTORY_SIZE = 5
    }

    // User adds API Key, saves to DataStore
    fun saveApiKey(key: String) {
        viewModelScope.launch {
            apiKeyDataStore.saveApiKey(key)
            _apiKeyHistory.update { currentList ->
                listOf(key) + currentList.filter { it != key }.take(MAX_HISTORY_SIZE - 1)
            }
        }
    }
}