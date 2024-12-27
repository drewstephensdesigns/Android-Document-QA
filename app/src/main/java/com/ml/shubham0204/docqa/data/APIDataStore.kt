package com.ml.shubham0204.docqa.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

val Context.dataStore by preferencesDataStore("api_key_prefs")
@Single
class APIDataStore(private val context: Context) {

    private val API_KEY = stringPreferencesKey("api_key")

    val apiKey: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[API_KEY]
    }

    suspend fun saveApiKey(key: String){
        context.dataStore.edit { prefs ->
            prefs[API_KEY] = key
        }
    }
}