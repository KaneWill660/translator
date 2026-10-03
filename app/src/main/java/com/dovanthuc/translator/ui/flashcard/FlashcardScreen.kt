package com.dovanthuc.translator.ui.flashcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dovanthuc.translator.data.repository.TranslationCardRepository
import com.dovanthuc.translator.ui.flashcard.components.ComparisonCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    repository: TranslationCardRepository,
    onBack: () -> Unit
) {
    val viewModel: FlashcardViewModel = viewModel(
        factory = viewModelFactory { initializer { FlashcardViewModel(repository) } }
    )
    val uiState by viewModel.uiState.collectAsState()
    val hasCard = uiState.current != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Luyện dịch") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            if (hasCard) {
                FlashcardBottomBar(
                    canGoPrev = uiState.canGoPrev,
                    onPrev = viewModel::loadPrev,
                    onNext = viewModel::loadNext
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.history.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.isEmpty -> {
                    Text(
                        text = "Chưa có câu nào để luyện tập.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
                uiState.current != null -> {
                    FlashcardContent(
                        entry = uiState.current!!,
                        onInputChanged = viewModel::onInputChanged,
                        onCheck = viewModel::onCheck
                    )
                }
            }
        }
    }
}

/** Fixed at the bottom of the screen (via Scaffold's bottomBar) so Prev/Next stay in the
 *  same easy-to-reach spot regardless of how long the card/comparison content above is. */
@Composable
private fun FlashcardBottomBar(
    canGoPrev: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onPrev, enabled = canGoPrev) {
                Text("◀ Prev")
            }
            Button(onClick = onNext) {
                Text("Next ▶")
            }
        }
    }
}

@Composable
private fun FlashcardContent(
    entry: FlashcardEntry,
    onInputChanged: (String) -> Unit,
    onCheck: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = entry.card.topic,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!entry.card.formula.isNullOrBlank() || !entry.card.hint.isNullOrBlank()) {
            HintSection(formula = entry.card.formula, hint = entry.card.hint)
        }

        Text(
            text = entry.card.vietnameseSentence,
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = entry.userInput,
            onValueChange = onInputChanged,
            label = { Text("Nhập bản dịch của bạn") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        Button(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onCheck()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kiểm tra")
        }

        if (entry.isSubmitted) {
            ComparisonCard(
                vietnameseSentence = entry.card.vietnameseSentence,
                sampleAnswer = entry.card.sampleAnswer,
                userInput = entry.userInput
            )
        }
    }
}

@Composable
private fun HintSection(formula: String?, hint: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        formula?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        hint?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
