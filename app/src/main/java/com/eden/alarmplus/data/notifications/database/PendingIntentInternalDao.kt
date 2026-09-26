package com.eden.alarmplus.data.notifications.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction

@Dao
interface PendingIntentInternalDao: PendingIntentDao {

    @Query(
        """
            WITH
                pending_requests_of_tag_cte AS (
                    SELECT *
                    FROM pending_intents
                    WHERE custom_tag = :customTag
                ),
                request_code_falling_edges_cte AS (
                    SELECT request_code+1 AS request_code
                    FROM pending_requests_of_tag_cte
                    EXCEPT
                    SELECT request_code
                    FROM pending_requests_of_tag_cte
                )
            SELECT request_code
            FROM request_code_falling_edges_cte
            ORDER BY request_code ASC
            LIMIT 1
        """
    )
    suspend fun internalFindMinAvailRequestCode(customTag: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun internalInsert(pendingIntent: PendingIntentEntity)

    @Transaction
    override suspend fun createRequestCode(customTag: Int, identifyingValue: String): Int {
        val availRequestCode = internalFindMinAvailRequestCode(customTag)
        internalInsert(PendingIntentEntity(
            customTag = customTag,
            requestCode = availRequestCode,
            identifyingValue = identifyingValue,
        ))
        return availRequestCode
    }

    @Transaction
    @Query(
        """
            DELETE FROM pending_intents
            WHERE
                custom_tag = :customTag
                AND
                identifying_value = :identifyingValue
        """
    )
    override suspend fun releaseRequestCode(customTag: Int, identifyingValue: String)
}