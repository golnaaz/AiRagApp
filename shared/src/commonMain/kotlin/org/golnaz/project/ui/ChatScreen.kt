package org.golnaz.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun ChatScreen(viewModel: ChatViewModel = chatViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val pickFile = rememberFilePicker { fileName, bytes ->
        viewModel.uploadDocument(fileName, bytes)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Upload section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Document", style = MaterialTheme.typography.titleSmall)
                state.uploadedFileName?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                state.uploadStatus?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { pickFile() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isUploading
                ) {
                    if (state.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (state.uploadedFileName == null) "Upload PDF" else "Replace PDF")
                    }
                }
            }
        }

        // Question input
        OutlinedTextField(
            value = state.questionInput,
            onValueChange = viewModel::onQuestionChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Type your question") }
        )

        Button(
            onClick = viewModel::askQuestion,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isAsking && state.docId != null
        ) {
            if (state.isAsking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Send to AI")
            }
        }

        // Answer card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(text = state.aiAnswer)
            }
        }
    }
}