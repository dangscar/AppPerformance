package com.nlhd.appperformance.Domain.Entity

import android.net.Uri

data class VideoStore(
    val uri: Uri,
    val name: String,
    val duration: Long,
    val size: Long
)
