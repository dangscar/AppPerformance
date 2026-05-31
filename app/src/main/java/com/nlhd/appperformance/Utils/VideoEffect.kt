package com.nlhd.appperformance.Utils

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer

data class AudioEffects(
    val equalizer: Equalizer,
    val bassBoost: BassBoost,
    val loudnessEnhancer: LoudnessEnhancer
)