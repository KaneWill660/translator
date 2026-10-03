package com.dovanthuc.translator.ui.flashcard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ComparisonCard(
    vietnameseSentence: String,
    sampleAnswer: String,
    userInput: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ComparisonRow(emoji = "🇻🇳", label = "Câu gốc", text = vietnameseSentence)
            ComparisonRow(emoji = "✅", label = "Đáp án mẫu", text = sampleAnswer)
            ComparisonRow(
                emoji = "✍️",
                label = "Bạn đã dịch",
                text = userInput.ifBlank { "(chưa tự dịch)" }
            )
        }
    }
}

@Composable
private fun ComparisonRow(emoji: String, label: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "$emoji $label:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
