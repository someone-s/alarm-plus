package com.eden.alarmplus.ui.main.welcome

import androidx.lifecycle.ViewModel
import com.eden.alarmplus.data.version.repository.VersionRepository
import kotlinx.coroutines.flow.Flow

class WelcomeViewModel(
    private val versionRepository: VersionRepository
): ViewModel() {
    val appName: String = versionRepository.appName
    val versionName: String = versionRepository.versionName
    val isVersionCodeGreaterThanStored: Flow<Boolean?> = versionRepository.isVersionCodeGreaterThanStored
    suspend fun storeBuildVersionCode() { versionRepository.storeBuildVersionCode() }
}