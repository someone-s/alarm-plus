package com.eden.alarmplus.data.alarms.repository

import androidx.annotation.CallSuper
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface GroupAlarmsRepository {

    /**
     * Must call super at the end of block to notify changes
     * @param group to add
     */
    @CallSuper
    suspend fun insertOrReplace(group: GroupData) {
        notify(ChangeNotification.Update(group.info.uuid))
    }

    /**
     * Must call super at the end of block to notify changes
     * @param groupUuid of group to toggle
     * @param enabled value to set
     */
    @CallSuper
    suspend fun toggle(groupUuid: Uuid, enabled: Boolean) {
        notify(ChangeNotification.Update(groupUuid))
    }

    /**
     * Must call super at the end of block to notify changes
     * @param groupUuid of group to delete
     */
    @CallSuper
    suspend fun delete(groupUuid: Uuid) {
        notify(ChangeNotification.Delete(groupUuid))
    }

    /**
     * This is called by [insertOrReplace], [toggle], and [delete]
     */
    suspend fun notify(notification: ChangeNotification)

    sealed class ChangeNotification {
        data class Update(val groupUuid: Uuid): ChangeNotification()
        data class Delete(val groupUuid: Uuid): ChangeNotification()
    }

    fun getAllGroupsStream(): Flow<List<GroupData>>
}