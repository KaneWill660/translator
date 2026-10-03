package com.dovanthuc.translator.ui.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dovanthuc.translator.data.repository.TranslationCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FlashcardViewModel(
    private val repository: TranslationCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    init {
        loadNext()
    }

    fun loadNext() {
        val state = _uiState.value

        // Already navigated back with Prev — just step forward through existing history.
        if (state.currentIndex < state.history.lastIndex) {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val excludeId = state.history.lastOrNull()?.card?.id
            val card = repository.getRandomCard(excludeId)
            if (card == null) {
                _uiState.update { it.copy(isLoading = false, isEmpty = true) }
                return@launch
            }
            _uiState.update {
                val newHistory = it.history + FlashcardEntry(card = card)
                it.copy(
                    history = newHistory,
                    currentIndex = newHistory.lastIndex,
                    isLoading = false,
                    isEmpty = false
                )
            }
        }
    }

    fun loadPrev() {
        _uiState.update {
            if (it.currentIndex > 0) it.copy(currentIndex = it.currentIndex - 1) else it
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { state ->
            state.copy(history = state.history.mapIndexed { index, entry ->
                if (index == state.currentIndex) entry.copy(userInput = text) else entry
            })
        }
    }

    fun onCheck() {
        _uiState.update { state ->
            state.copy(history = state.history.mapIndexed { index, entry ->
                if (index == state.currentIndex) entry.copy(isSubmitted = true) else entry
            })
        }
    }
}
