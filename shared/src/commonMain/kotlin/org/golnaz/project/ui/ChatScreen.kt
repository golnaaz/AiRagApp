package org.golnaz.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.golnaz.project.repository.AiRepository
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    var questionInput by remember { mutableStateOf("") }
    var aiAnswer by remember { mutableStateOf("Ask a question from your PDF...") }
    var isLoading by remember { mutableStateOf(false) }

    val repository = remember { AiRepository() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = questionInput,
            onValueChange = { questionInput = it },
            modifier = Modifier.padding(top =60.dp).fillMaxWidth(),
            label = { Text("Type your question") }
        )

        Button(
            onClick = {
                if (questionInput.isNotBlank()) {
                    isLoading = true
                    coroutineScope.launch {
                        val result = repository.askQuestion(questionInput)
                        isLoading = false
                        aiAnswer = result.fold(
                            onSuccess = { it.answer },
                            onFailure = { "Error: ${it.message}" }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Send to AI")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(text = aiAnswer)
            }
        }
    }
}
