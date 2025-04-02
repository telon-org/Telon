package net.nativemind.telon

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class Playback {
    companion object {
        fun play(filePath: Path, audioFormat: AudioFormat) {
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
            val fileChannel: FileChannel = FileChannel.open(filePath, StandardOpenOption.READ)
            var bytesRead: Int
            audioTrack.play()
            while (true) {
                audioByteBuffer.clear()
                bytesRead = fileChannel.read(audioByteBuffer)
                Log.v(Playback::class.java.simpleName, "Bytes read: $bytesRead")
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
}
