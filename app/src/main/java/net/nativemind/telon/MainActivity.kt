package net.nativemind.telon

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var recStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialized view
        recStatus = findViewById(R.id.recStatus)

        // Initialize audio recorder
        val audioFormat: AudioFormat = Recording.getBestAudioFormat()
        val audioRecord: AudioRecord? =
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                Recording.initialize(audioFormat)
            } else null
        Recording.fetchRecordingStatus(this, recStatus, audioRecord)

        // Bind button to onClickListener
        // Recording
        findViewById<Button>(R.id.btnRecStart).setOnClickListener {
            audioRecord?.startRecording()
            // TODO: Record audio to file
            Recording.fetchRecordingStatus(this, recStatus, audioRecord)
        }
        findViewById<Button>(R.id.btnRecStop).setOnClickListener {
            audioRecord?.stop()
            Recording.fetchRecordingStatus(this, recStatus, audioRecord)
        }
        // TODO: Call
    }
}