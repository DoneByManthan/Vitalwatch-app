package com.vitalwatch.app.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalwatch.app.domain.model.Reading
import com.vitalwatch.app.domain.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val readingRepository: ReadingRepository
) : ViewModel() {

    val readings: StateFlow<List<Reading>> = readingRepository
        .getAllReadings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
