package com.ml.shubham0204.docqa.ui.screens.editAPI

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ml.shubham0204.docqa.ui.theme.DocQATheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAPIScreen(
    viewModel: ApiKeyViewModel = viewModel(),
    onBackClick: (() -> Unit)
) {
    DocQATheme {
        val apiKey by viewModel.apiKey.collectAsState("")
        val coroutineScope = rememberCoroutineScope()
        var userApiKey by remember { mutableStateOf("") }
        val snackbarHostState = remember { SnackbarHostState() }
        val recentlyUsedApiKeys by viewModel.apiKeyHistory.collectAsState(emptyList())

        Scaffold(
            snackbarHost = {SnackbarHost(snackbarHostState)},
            topBar = {
                TopAppBar(
                    title = {
                        Text("Add API Key",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate Back",
                            )
                        }
                    },
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                //Text("Enter your API Key", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = userApiKey,
                    onValueChange = { userApiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                             viewModel.saveApiKey(userApiKey)
                            Log.e("API KEY CHANGED", "API Key: $userApiKey")
                             snackbarHostState.showSnackbar("API Key Saved")
                            //navController.navigate("chat"){
                            //    popUpTo("editAPI"){inclusive = true}
                            //}
                        }

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save API Key")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recently Used Keys:")
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(recentlyUsedApiKeys) { key ->
                        Text(text= key, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}