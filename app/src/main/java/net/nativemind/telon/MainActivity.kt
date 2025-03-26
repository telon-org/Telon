package net.nativemind.telon

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.io.path.Path

class MainActivity : AppCompatActivity() {

    private lateinit var recStatus: TextView
    private lateinit var recording: Recording

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialized view
        recStatus = findViewById(R.id.recStatus)

        // Initialize audio recorder
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            recording = Recording(Recording.getBestAudioFormat())
            recording.setStatusView(this, recStatus)
            // Bind button to onClickListener
            findViewById<Button>(R.id.btnRecStart).setOnClickListener {
                recording.recordToFile(Path(filesDir.toString(), "temp.pcm"))
            }
            findViewById<Button>(R.id.btnRecStop).setOnClickListener {
                recording.stopRecord()
            }
            findViewById<Button>(R.id.btnRecPlayback).setOnClickListener {
                Log.d(packageName, fileList()[0].toString())
                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(48000)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
                val bufferSizeInBytes = 2 * AudioTrack.getMinBufferSize(
                    audioFormat.sampleRate,
                    audioFormat.channelMask,
                    audioFormat.encoding
                )
                val audioTrack: AudioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .build()
                val audioByteBuffer: ByteBuffer =
                    ByteBuffer.allocateDirect(bufferSizeInBytes)
                val fileChannel: FileChannel = openFileInput("temp.pcm").channel
                var bytesRead: Int
                while (true) {
                    audioByteBuffer.clear()
                    bytesRead = fileChannel.read(audioByteBuffer)
                    Log.v(packageName, "Bytes read: $bytesRead")
                    if (bytesRead <= 0) {
                        break
                    }
                    audioByteBuffer.flip()
                    audioTrack.write(audioByteBuffer, bytesRead, AudioTrack.WRITE_BLOCKING)
                }
                fileChannel.close()
                audioTrack.play()
            }
        }
        // TODO: Call
    }
}