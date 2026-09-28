package com.eden.alarmplus.data.alarms.structure.audio

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("audio-data")
@Parcelize
class AudioData(
    @SerialName("name")
    val name: String,
    @SerialName("uri")
    internal val uriString: String,
) : Parcelable {

    constructor(
        name: String,
        uri: Uri,
    ) : this(
        name = name,
        uriString = uri.toString(),
    )

    val uri: Uri get() = uriString.toUri()

}