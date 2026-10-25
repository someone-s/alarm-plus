package com.eden.alarmplus.ui.main.about.support

import androidx.lifecycle.ViewModel
import com.eden.alarmplus.data.version.repository.VersionRepository

class SupportViewModel(
    versionRepository: VersionRepository
): ViewModel() {
    val applicationId: String = versionRepository.applicationId
    val versionName: String = versionRepository.versionName
    val versionCode: Int = versionRepository.versionCode
}