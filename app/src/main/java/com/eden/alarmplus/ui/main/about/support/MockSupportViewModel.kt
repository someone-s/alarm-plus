package com.eden.alarmplus.ui.main.about.support

import com.eden.alarmplus.data.version.repository.VersionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

val mockSupportViewModel = SupportViewModel(
    versionRepository = object : VersionRepository {
        override val appName: String = "App Name"
        override val applicationId: String = "com.placeholder.appid"
        override val versionName: String = "0.0.0"
        override val versionCode: Int = 0
        override val isVersionCodeGreaterThanStored: Flow<Boolean?> = flowOf(true)
        override suspend fun storeBuildVersionCode() {}
    }
)