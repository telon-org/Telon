package net.nativemind.telon

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import net.nativemind.telon.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.io.path.Path

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recording: Recording

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        // Initialize audio recorder
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            binding.recStatus.text = resources.getString(R.string.permission_not_granted)
        } else {
            recording = Recording(Recording.getBestAudioFormat())
            recording.setStatusView(this, binding.recStatus)
            // Bind button to onClickListener
            binding.btnRecStart.setOnClickListener {
                recording.recordToFile(Path(filesDir.toString(), "temp.pcm"))
            }
            binding.btnRecStop.setOnClickListener {
                recording.stopRecord()
            }
            binding.btnRecPlayback.setOnClickListener {
                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(48000)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
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
                audioTrack.play()
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
                audioTrack.stop()
                audioTrack.release()
            }
        }
        // TODO: Call
    }
}