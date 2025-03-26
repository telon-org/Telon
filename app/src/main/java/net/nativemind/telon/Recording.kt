package net.nativemind.telon

import android.app.Activity
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresPermission
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.EnumSet

class Recording// Build and return audio record
@RequiresPermission(value = "android.permission.RECORD_AUDIO")
constructor(audioFormat: AudioFormat) {
    private var audioRecord: AudioRecord
    private lateinit var statusView: TextView
    private lateinit var activity: Activity
    private var bufferSizeInBytes: Int = 0
    private var continueRecording: Boolean = false

    init {
        bufferSizeInBytes = 2 * AudioRecord.getMinBufferSize(
            audioFormat.sampleRate,
            audioFormat.channelMask,
            audioFormat.encoding
        )
        Log.d(Recording::class.java.simpleName, "Audio buffer size: $bufferSizeInBytes")
        audioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSizeInBytes)
            .build()
    }

    fun setStatusView(activity: Activity, statusView: TextView) {
        this.statusView = statusView
        this.activity = activity
        fetchRecordingStatus()
    }

    fun recordToFile(filePath: Path) {
        if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            val audioByteBuffer = ByteBuffer.allocateDirect(bufferSizeInBytes)
            val fileOutputChannel = FileChannel.open(
                filePath, EnumSet.of(
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
                )
            )
            audioRecord.startRecording()
            continueRecording = true
            fetchRecordingStatus()
            Thread {
                while (continueRecording) {
                    audioByteBuffer.clear()
                    audioRecord.read(audioByteBuffer, bufferSizeInBytes, AudioRecord.READ_BLOCKING)
                    Log.v(javaClass.simpleName, "Got message: $audioByteBuffer")
                    fileOutputChannel.write(audioByteBuffer)
                }

                audioRecord.stop()
                fileOutputChannel.close()

                activity.runOnUiThread {
                    fetchRecordingStatus()
                }
                Log.d(javaClass.simpleName, "Closing")
            }.start()
        }
    }

    fun stopRecord() {
        continueRecording = false
    }

    private fun fetchRecordingStatus() {
        if (audioRecord.state == AudioRecord.STATE_UNINITIALIZED) {
            statusView.text = this.activity.resources.getString(R.string.uninitalized)
            statusView.setTextColor(Color.RED)
            return
        }
        when (audioRecord.recordingState) {
            AudioRecord.RECORDSTATE_RECORDING -> {
                statusView.text = this.activity.resources.getString(R.string.active)
                statusView.setTextColor(Color.RED)
            }

            AudioRecord.RECORDSTATE_STOPPED -> {
                statusView.text = this.activity.resources.getString(R.string.inactive)
                statusView.setTextColor(Color.BLACK)
            }
        }
    }

    companion object {
        fun getBestAudioFormat(): AudioFormat {
            // TODO: Check for compatible sample rate
            return AudioFormat.Builder()
                .setSampleRate(48000)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build()
        }
    }
}