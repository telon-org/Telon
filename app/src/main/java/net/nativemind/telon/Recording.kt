package net.nativemind.telon

import android.content.Context
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.widget.TextView
import androidx.annotation.RequiresPermission

class Recording {
    companion object {
        fun getBestAudioFormat(): AudioFormat {
            // TODO: Check for compatible sample rate
            return AudioFormat.Builder()
                .setSampleRate(48000)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                .build()
        }

        @RequiresPermission(value = "android.permission.RECORD_AUDIO")
        fun initialize(audioFormat: AudioFormat): AudioRecord {
            // Build and return audio record
            val minBufSize = AudioRecord.getMinBufferSize(
                audioFormat.sampleRate,
                audioFormat.channelMask,
                audioFormat.encoding
            )
            val audioRecord =
                AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(2 * minBufSize)
                    .build()
            return audioRecord
        }
        // TODO: Record audio to file method

        fun fetchRecordingStatus(
            context: Context,
            textStatus: TextView,
            audioRecord: AudioRecord?
        ) {
            if (audioRecord == null || audioRecord.state == AudioRecord.STATE_UNINITIALIZED) {
                textStatus.text = context.resources.getString(R.string.uninitalized)
                textStatus.setTextColor(Color.RED)
                return
            }
            when (audioRecord.recordingState) {
                AudioRecord.RECORDSTATE_RECORDING -> {
                    textStatus.text = context.resources.getString(R.string.active)
                    textStatus.setTextColor(Color.RED)
                }

                AudioRecord.RECORDSTATE_STOPPED -> {
                    textStatus.text = context.resources.getString(R.string.inactive)
                    textStatus.setTextColor(Color.BLACK)
                }
            }
        }
    }
}