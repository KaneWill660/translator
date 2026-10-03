package com.dovanthuc.translator.ui.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dovanthuc.translator.data.remote.openai.OpenAiRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTranslateScreen(
    openAiRepository: OpenAiRepository,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val speechRecognizerManager = remember { SpeechRecognizerManager(context.applicationContext) }
    val viewModel: VoiceTranslateViewModel = viewModel(
        factory = viewModelFactory {
            initializer { VoiceTranslateViewModel(openAiRepository, speechRecognizerManager) }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) viewModel.startListening()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dịch bằng giọng nói") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FilledIconButton(
                onClick = {
                    when {
                        uiState.isListening -> viewModel.finishListening()
                        uiState.isProcessingStop -> Unit // đang chờ phiên trước đóng hẳn, bỏ qua tap
                        hasMicPermission -> viewModel.startListening()
                        else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                enabled = !uiState.isProcessingStop,
                modifier = Modifier.padding(top = 16.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (uiState.isListening)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isProcessingStop) {
                    CircularProgressIndicator(modifier = Modifier.padding(14.dp))
                } else {
                    Icon(
                        imageVector = if (uiState.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (uiState.isListening) "Bấm để dừng" else "Nhấn để nói",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            Text(
                when {
                    uiState.isProcessingStop -> "Đang xử lý..."
                    uiState.isListening -> "Đang nghe... (bấm để dừng)"
                    else -> "Nhấn để nói"
                }
            )

            uiState.error?.let { errorMessage ->
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                if (errorMessage.contains("API Key")) {
                    Button(onClick = onOpenSettings) {
                        Text("Mở Cài đặt")
                    }
                }
            }

            Text("Bạn vừa nói (có thể sửa):", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = uiState.recognizedText,
                onValueChange = viewModel::onRecognizedTextChanged,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Button(
                onClick = viewModel::translate,
                enabled = uiState.recognizedText.isNotBlank() && !uiState.isTranslating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dịch sang Anh")
            }

            if (uiState.isTranslating) {
                CircularProgressIndicator()
            }

            if (uiState.translatedText.isNotBlank()) {
                Text("Kết quả dịch (AI):", style = MaterialTheme.typography.labelMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = uiState.translatedText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
