package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiResult
import com.example.data.local.MessageEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.SageDatabase
import com.example.data.local.TopicEntity
import com.example.data.repository.SageRepository
import com.example.util.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConnectionStatus(
    val isChecking: Boolean = false,
    val isSuccess: Boolean? = null,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SageViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val geminiClient = GeminiClient(
        backendUrlProvider = { preferencesManager.customBackendUrl.ifEmpty { null } }
    )
    private val database = SageDatabase.getInstance(application)
    private val repository = SageRepository(
        dao = database.sageDao(),
        geminiClient = geminiClient,
        preferencesManager = preferencesManager
    )
    private val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, networkMonitor.isCurrentlyConnected())

    val allTopics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _activeTopic = MutableStateFlow<TopicEntity?>(null)
    val activeTopic: StateFlow<TopicEntity?> = _activeTopic.asStateFlow()

    val messages: StateFlow<List<MessageEntity>> = _activeTopic.flatMapLatest { topic ->
        if (topic != null) repository.getMessagesForTopic(topic.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _connectionStatus = MutableStateFlow(ConnectionStatus())
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentMode = MutableStateFlow("NORMAL")
    val currentMode: StateFlow<String> = _currentMode.asStateFlow()

    val streakDays: Int
        get() = preferencesManager.streakDays

    val lastLatencyMs: Long
        get() = preferencesManager.lastLatencyMs

    val lastErrorMessage: String
        get() = preferencesManager.lastErrorMessage

    val lastRequestSuccess: Boolean
        get() = preferencesManager.lastRequestSuccess

    init {
        viewModelScope.launch {
            val topic = repository.getOrCreateDefaultTopic()
            _activeTopic.value = topic
            _currentMode.value = topic.mode
        }
        testAiConnection()
    }

    fun testAiConnection() {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus(isChecking = true)
            when (val result = repository.testConnection()) {
                is GeminiResult.Success -> {
                    _connectionStatus.value = ConnectionStatus(
                        isChecking = false,
                        isSuccess = true
                    )
                }
                is GeminiResult.Error -> {
                    _connectionStatus.value = ConnectionStatus(
                        isChecking = false,
                        isSuccess = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun setMode(mode: String) {
        _currentMode.value = mode
        val current = _activeTopic.value ?: return
        viewModelScope.launch {
            val updated = current.copy(mode = mode)
            repository.updateTopic(updated)
            _activeTopic.value = updated
        }
    }

    fun selectTopic(topic: TopicEntity) {
        _activeTopic.value = topic
        _currentMode.value = topic.mode
        preferencesManager.activeTopicId = topic.id
    }

    fun createTopic(title: String, mode: String) {
        viewModelScope.launch {
            val created = repository.createTopic(title, mode)
            _activeTopic.value = created
            _currentMode.value = created.mode
        }
    }

    fun deleteTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
            if (_activeTopic.value?.id == topic.id) {
                val fallback = repository.getOrCreateDefaultTopic()
                _activeTopic.value = fallback
                _currentMode.value = fallback.mode
            }
        }
    }

    fun sendMessage(text: String) {
        val topic = _activeTopic.value ?: return
        if (text.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                repository.sendMessage(
                    topicId = topic.id,
                    userText = text,
                    currentMode = _currentMode.value
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun retryMessage(messageId: Long) {
        val topic = _activeTopic.value ?: return
        if (_isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                repository.retryMessage(
                    topicId = topic.id,
                    failedMessageId = messageId,
                    currentMode = _currentMode.value
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    val backendUrl: String
        get() = preferencesManager.customBackendUrl.ifEmpty { geminiClient.getEffectiveBackendUrl() }

    fun saveBackendUrl(url: String) {
        preferencesManager.customBackendUrl = url.trim()
        testAiConnection()
    }
}
