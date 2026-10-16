package com.eden.alarmplus.alert

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.eden.alarmplus.BuildConfig
import com.eden.alarmplus.alert.hapticaudio.HapticAudioScope
import com.eden.alarmplus.alert.notification.AlertNotificationScope
import com.eden.alarmplus.alert.notification.ForewarnNotificationScope
import com.eden.alarmplus.alert.vibration.VibrationScope
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.VibrateFeature
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.notifications.repository.NotificationRepository
import com.eden.alarmplus.data.notifications.structure.AlertState
import com.eden.alarmplus.data.notifications.structure.ForewarnState
import com.eden.alarmplus.logic.access.DismissReceiver
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class AlertLogic(
    private val service: AlertService,
    private val notificationRepository: NotificationRepository,
    private val alertNotificationScope: AlertNotificationScope,
    private val forewarnNotificationScope: ForewarnNotificationScope,
    private val hapticAudioScope: HapticAudioScope,
    private val vibrationScope: VibrationScope,
) {
    private val notificationManager = NotificationManagerCompat.from(service)

    companion object {

        /**
         * Must be started as a foreground service;
         * Must contain the following extras:
         * - [EXTRA_GROUP_INFO]
         * - [EXTRA_ALARM_DATA]
         * - [EXTRA_IS_LAST_IN_GROUP]
         */
        const val ACTION_ALERT = "${BuildConfig.APPLICATION_ID}.ACTION_ALERT"

        /**
         * Must be started as a regular service;
         * Must contain the following extras:
         * - [EXTRA_GROUP_INFO]
         */
        const val ACTION_FOREWARN = "${BuildConfig.APPLICATION_ID}.ACTION_FOREWARN"

        /**
         * Must be started as a regular service;
         * No extras required
         */
        const val ACTION_CLEAR_FOREWARN = "${BuildConfig.APPLICATION_ID}.ACTION_CLEAR_FOREWARN"

        /**
         * Extra of type [Parcelable] that is [com.eden.alarmplus.data.alarms.structure.group.GroupInfo]
         */
        const val EXTRA_GROUP_INFO = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_INFO"

        /**
         * Extra of type [Parcelable] that is [com.eden.alarmplus.data.alarms.structure.alarm.AlarmData]
         */
        const val EXTRA_ALARM_DATA = "${BuildConfig.APPLICATION_ID}.EXTRA_ALARM_DATA"

        /**
         * Extra of type [Boolean]
         */
        const val EXTRA_IS_LAST_IN_GROUP = "${BuildConfig.APPLICATION_ID}.EXTRA_IS_LAST_IN_GROUP"


        /**
         * Must be started as a regular service;
         * Must contain the following extras:
         * - [EXTRA_GROUP_UUID]
         */
        const val ACTION_SNOOZE = "${BuildConfig.APPLICATION_ID}.ACTION_SNOOZE"

        /**
         * Must be started as a regular service;
         * Must contain the following extras:
         * - [EXTRA_GROUP_UUID]
         */
        const val ACTION_DISMISS = "${BuildConfig.APPLICATION_ID}.ACTION_DISMISS"
        const val EXTRA_GROUP_UUID = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_UUID"

        private val FOREGROUND_SERVICE_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
        else 0

    }

    /**
     * @param startId identifies request
     * @param intent have one of the following actions:
     *  - [ACTION_FOREWARN]
     *  - [ACTION_ALERT]
     *  - [ACTION_SNOOZE]
     *  - [ACTION_DISMISS]
     *  - [ACTION_CLEAR_FOREWARN]
     */
    fun processRequest(startId: Int, intent: Intent) {
        intent.dataString
        when (intent.action) {
            ACTION_FOREWARN -> processForewarn(
                serviceStartId = startId,
                notificationId = startId,
                groupInfo = IntentCompat.getParcelableExtra(
                    intent, EXTRA_GROUP_INFO, GroupInfo::class.java)
                    ?: throw IllegalArgumentException("Intent missing group info"),
            )
            ACTION_ALERT -> processAlert(
                serviceStartId = startId,
                notificationId = startId,
                groupInfo = IntentCompat.getParcelableExtra(
                    intent, EXTRA_GROUP_INFO, GroupInfo::class.java)
                    ?: throw IllegalArgumentException("Intent missing group info"),
                alarmData = IntentCompat.getParcelableExtra(
                    intent, EXTRA_ALARM_DATA, AlarmData::class.java
                ) ?: throw IllegalArgumentException("Intent missing alarm data."),
                isLastInGroup = intent.getBooleanExtra(
                    EXTRA_IS_LAST_IN_GROUP, false
                )
            )
            ACTION_SNOOZE -> processSnooze(
                serviceStartId = startId,
                groupUuid = IntentCompat.getSerializableExtra(
                    intent, EXTRA_GROUP_UUID, Uuid::class.java
                ) ?: throw IllegalArgumentException("Intent missing group uuid."),
            )
            ACTION_DISMISS -> processDismiss(
                serviceStartId = startId,
                groupUuid = IntentCompat.getSerializableExtra(
                    intent, EXTRA_GROUP_UUID, Uuid::class.java
                ) ?: throw IllegalArgumentException("Intent missing group uuid."),
            )
            ACTION_CLEAR_FOREWARN -> processClearForewarn(
                serviceStartId = startId,
            )
            else -> throw IllegalArgumentException("Intent action not expected values.")
        }
    }

    private fun processForewarn(
        serviceStartId: Int,
        notificationId: Int,
        groupInfo: GroupInfo
    ) {

        // Exit if no permission to send notifications
        if (ContextCompat.checkSelfPermission(
                service,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("$javaClass", "Missing POST_NOTIFICATIONS permission")
            return
        }

        synchronized(this@AlertLogic) {
            startForewarnNotification(notificationId, groupInfo)
        }

        service.stopSelf(serviceStartId)
    }

    private fun processClearForewarn(
        serviceStartId: Int,
    ) {
        synchronized(this@AlertLogic) {

            val groupUuids = runBlocking { notificationRepository.getForewarns() }
                .map { it.groupUuid }
            for (groupUuid in groupUuids)
                cancelForewarnNotification(groupUuid)
        }

        service.stopSelf(serviceStartId)
    }

    private fun processAlert(
        serviceStartId: Int,
        notificationId: Int,
        groupInfo: GroupInfo,
        alarmData: AlarmData,
        isLastInGroup: Boolean,
    ) {

        // Exit if no permission to send notifications
        if (ContextCompat.checkSelfPermission(
                service,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            Log.w("$javaClass", "Missing POST_NOTIFICATIONS permission")
            return
        }

        synchronized(this@AlertLogic) {

            cancelForewarnNotification(groupInfo.uuid)

            startAlertNotification(notificationId, groupInfo, alarmData, isLastInGroup)

            cancelAlertNotification(serviceStartId, groupInfo.uuid, notificationId)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun startForewarnNotification(
        notificationId: Int,
        groupInfo: GroupInfo,
    ) {
        val previous = runBlocking { notificationRepository.getForewarn(groupInfo.uuid) }
        if (previous != null)
            cancelForewarnNotification(groupInfo.uuid)

        val notification = with(notificationManager) {
            forewarnNotificationScope.Builder(groupInfo).apply {

                addDismissIntent(
                    PendingIntent.getBroadcast(
                        service,
                        notificationId,
                        Intent(service, DismissReceiver::class.java).apply {
                            putExtra(DismissReceiver.EXTRA_GROUP_UUID, groupInfo.uuid)
                        },
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

            }.build(this@with)
        }

        val state = ForewarnState(
            groupUuid = groupInfo.uuid,
            notificationId = notificationId,
        )

        runBlocking { notificationRepository.setForewarn(state) }

        with(notificationManager)  {
            notify(notificationId, notification)
        }
    }

    private fun cancelForewarnNotification(
        groupUuid: Uuid,
    ) {
        val state = runBlocking { notificationRepository.getForewarn(groupUuid) }
        if (state == null) {
            Log.i("$javaClass", "Forewarn for group $groupUuid already cancelled.")
            return
        }

        forewarnNotificationScope.cancelNotification(
            state.notificationId,
            notificationManager,
        )

        runBlocking { notificationRepository.deleteForewarn(groupUuid) }
    }

    private fun startAlertNotification(
        notificationId: Int,
        groupInfo: GroupInfo,
        alarmData: AlarmData,
        isLastInGroup: Boolean,
    ) {
        if (runBlocking { notificationRepository.hasAnyAlerts() })
            ServiceCompat.stopForeground(
                service,
                ServiceCompat.STOP_FOREGROUND_DETACH
            )

        val state = AlertState(
            groupUuid = groupInfo.uuid,
            notificationId = notificationId,
            displayedName = groupInfo.name,
            displayedTime = alarmData.info.time,
            snoozeAllowed = !isLastInGroup,
            dismissAllowed = true,
            audioUri = alarmData.getFeature<AudioFeature>()?.data?.uri,
            vibrateEnabled = alarmData.getFeature<VibrateFeature>() != null
        )

        runBlocking { notificationRepository.addLastAlert(state) }

        playAlertNotification(state)
    }

    private fun playAlertNotification(alertState: AlertState) {

        val notification = with(notificationManager) {
            alertNotificationScope.Builder().apply {
                setTitle(alertState.displayedName)

                addTimeout(10.minutes.inWholeMilliseconds)

                if (alertState.snoozeAllowed)
                    addSnoozeIntent(
                        PendingIntent.getService(
                            service,
                            alertState.notificationId,
                            Intent(service, AlertService::class.java).apply {
                                action = ACTION_SNOOZE
                                putExtra(EXTRA_GROUP_UUID, alertState.groupUuid)
                            },
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )

                if (alertState.dismissAllowed)
                    addDismissIntent(
                        PendingIntent.getBroadcast(
                            service,
                            alertState.notificationId,
                            Intent(service, DismissReceiver::class.java).apply {
                                putExtra(DismissReceiver.EXTRA_GROUP_UUID, alertState.groupUuid)
                            },
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )

                if (canUseFullScreenIntent() && alertState.displayedTime != null)
                    addFullScreen(alertState.notificationId, alertState.displayedTime)

            }.build(this@with)
        }

        ServiceCompat.startForeground(
            service,
            alertState.notificationId,
            notification,
            FOREGROUND_SERVICE_TYPE
        )

        if (alertState.audioUri != null)
            try {
                hapticAudioScope.start(alertState.audioUri, alertState.vibrateEnabled)
            } catch (exception: Exception) {
                Log.e("$javaClass", exception.message, exception)
            }

        if (alertState.vibrateEnabled)
            try {
                vibrationScope.start(service.lifecycleScope)
            } catch (exception: Exception) {
                Log.e("$javaClass", exception.message, exception)
            }
    }

    private fun cancelAlertNotification(
        serviceStartId: Int,
        groupUuid: Uuid,
        exceptionNotificationId: Int? = null,
        notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(service),
    ) {
        val originalLastAlert = runBlocking { notificationRepository.getLastAlert() }

        val alertsToCancel = runBlocking { notificationRepository.getAlerts() }
            .filter { it.groupUuid == groupUuid }
            .filter { it.notificationId != exceptionNotificationId }
            .toList()

        // last notification is most recent notification
        val anyLastNotification = alertsToCancel.any { it.notificationId == (originalLastAlert?.notificationId) }

        if (anyLastNotification) {
            try {
                hapticAudioScope.stopIfExist()
            } catch (exception: Exception) {
                Log.e("$javaClass", exception.message, exception)
            }

            try {
                vibrationScope.stopIfExist()
            } catch (exception: Exception) {
                Log.e("$javaClass", exception.message, exception)
            }
        }

        // Remove notification from list
        runBlocking { alertsToCancel.forEach {
            notificationRepository.deleteAlert(it.groupUuid)
        } }

        fun List<AlertState>.cancelAll(): Unit = forEach {
            alertNotificationScope.cancelNotificationAndFullScreen(it.notificationId, notificationManager)
        }

        val alertsIsEmpty = !runBlocking {
            notificationRepository.hasAnyAlerts()
        }
        if (alertsIsEmpty) {

            // Stop foreground also cancel notification
            ServiceCompat.stopForeground(
                service,
                ServiceCompat.STOP_FOREGROUND_REMOVE
            )

            // Cancel notification
            alertsToCancel.cancelAll()

            service.stopSelf(serviceStartId)
        }
        else {
            if (anyLastNotification) {
                val newLastAlert = runBlocking { notificationRepository.getLastAlert() }

                // Bind new last notification, notification pushed again
                if (newLastAlert != null)
                    playAlertNotification(newLastAlert)
            }

            // Cancel notification
            alertsToCancel.cancelAll()
        }
    }

    private fun processSnooze(
        serviceStartId: Int,
        groupUuid: Uuid
    ) {
        synchronized(this@AlertLogic) {
            cancelAlertNotification(serviceStartId, groupUuid)
        }
    }

    private fun processDismiss(
        serviceStartId: Int,
        groupUuid: Uuid
    ) {
        synchronized(this@AlertLogic) {
            cancelForewarnNotification(groupUuid)

            cancelAlertNotification(serviceStartId, groupUuid)
        }
    }
}