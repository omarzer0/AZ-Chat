package az.zero.azchat.presentation.main.adapter.messages

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import az.zero.azchat.common.tryNow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AudioHandler @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : OnCompletionListener {
    ////                    val mediaPlayer = MediaPlayer()
    ////                    mediaPlayer.apply {
    ////                        tryNow {
    ////                            setDataSource(requireContext(), it)
    ////                            prepare()
    ////                            logMe("playing....")
    ////                            setVolume(1.0f, 1.0f)
    ////                            start()
    ////                        }
    ////                    }
    private var mediaPlayer: MediaPlayer? = null
    private var nowPlayingAudio: Uri? = null

    fun playAudio(audioPath: String) {
        tryNow {
            val audioUri = Uri.parse(audioPath)
//        if (audioPath == nowPlayingAudio) {
//            resumeAudio()
//        } else {
//            nowPlayingAudio = audioPath
            playNewAudio(audioUri)
        }
    }

    private fun playNewAudio(audioUri: Uri) {
        if (mediaPlayer == null) mediaPlayer = MediaPlayer()
        mediaPlayer?.apply {
            setDataSource(applicationContext, audioUri)
            prepare()
            setVolume(1.0f, 1.0f)
            start()
        }
    }

    private fun resumeAudio() {
        mediaPlayer?.start()
    }

    fun pauseAudio() {
        mediaPlayer?.pause()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mediaPlayer?.apply {
            release()
            mediaPlayer = null
        }
    }

    fun getTotalTimeForAudio(audioPath: String): Int {
        val uri = Uri.parse(audioPath)
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(applicationContext, uri)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr?.toInt() ?: 0
    }

}