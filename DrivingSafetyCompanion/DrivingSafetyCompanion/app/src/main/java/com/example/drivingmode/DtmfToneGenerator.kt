package com.example.drivingmode

import android.media.AudioManager
import android.media.ToneGenerator

class DtmfToneGenerator {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_DTMF, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playTone(digit: Char) {
        val tone = when (digit) {
            '1' -> ToneGenerator.TONE_DTMF_1
            '2' -> ToneGenerator.TONE_DTMF_2
            '3' -> ToneGenerator.TONE_DTMF_3
            '4' -> ToneGenerator.TONE_DTMF_4
            '5' -> ToneGenerator.TONE_DTMF_5
            '6' -> ToneGenerator.TONE_DTMF_6
            '7' -> ToneGenerator.TONE_DTMF_7
            '8' -> ToneGenerator.TONE_DTMF_8
            '9' -> ToneGenerator.TONE_DTMF_9
            '0' -> ToneGenerator.TONE_DTMF_0
            '*' -> ToneGenerator.TONE_DTMF_S
            '#' -> ToneGenerator.TONE_DTMF_P
            else -> null
        }

        tone?.let {
            toneGenerator?.startTone(it, 150)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
