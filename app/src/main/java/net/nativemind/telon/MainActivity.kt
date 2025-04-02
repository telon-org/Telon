package net.nativemind.telon

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import net.nativemind.telon.databinding.ActivityMainBinding
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
                Playback.play(Path(filesDir.toString(), "temp.pcm"), AudioFormat.Builder()
                        .setSampleRate(48000)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()
                )
            }
        }
        // TODO: Call
    }
}