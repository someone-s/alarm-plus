package com.eden.alarmplus.structure.audio

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("audio-data")
class AudioData {

    constructor(
        name: String,
        uri: Uri,
    ) {
        this.name = name
        this.uriString = uri.toString()
    }

    @SerialName("name")
    val name: String

    @SerialName("uri")
    internal val uriString: String

    val uri: Uri get() = uriString.toUri()

}