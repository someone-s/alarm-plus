package com.eden.alarmplus.logic.access

import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import kotlin.uuid.Uuid

/**
 * Use this or directly send intent to receivers
 */
class LocalStateHookProvider(
    private val context: Context
): StateHookProvider {

    override fun initializeState(dateTime: LocalDateTime) {
        context.sendBroadcast(
            Intent(context, InitializeReceiver::class.java).apply {
                putExtra(InitializeReceiver.EXTRA_DATE_TIME, dateTime)
            }
        )
    }

    override fun dismissGroup(groupUuid: Uuid) {
        context.sendBroadcast(
            Intent(context, DismissReceiver::class.java).apply {
                putExtra(DismissReceiver.EXTRA_GROUP_UUID, groupUuid)
            }
        )
    }
}