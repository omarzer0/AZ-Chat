package az.zero.azchat.common.audio.record

import android.Manifest
import android.media.MediaRecorder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import az.zero.azchat.common.IS_DEBUG
import az.zero.azchat.common.tryNow
import javax.inject.Singleton

@Singleton
class AudioRecorderHelper(
    private val fragment: Fragment,
    private val viewToListenAt: View,
    private val listener: AudioRecordListener
) {

    private var mRecorder: MediaRecorder? = null
    private val tag = "AudioRecorder"
    private var mLocalFilePath = ""

    private val defaultPath = "${fragment.activity?.externalCacheDir?.absolutePath}"

    //        private val defaultPath =
//        fragment.activity?.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath + "/recording.3gp"
    private var relativePath = "out"
    private var fileExtension = "3gp"
    private var startTimerCount = -1L
    private var endTimerCount = -1L


    private fun startRecording() {
        tryNow {
            mRecorder = MediaRecorder()
            mLocalFilePath =
                "$defaultPath/$relativePath${System.currentTimeMillis()}.$fileExtension"
            mRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(mLocalFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
                startCount()
                logMe("started", tag)
            }
        }
    }

    private fun stopRecording(
        onRecorderSuccess: (audioFilePath: String, duration: Long) -> Unit,
    ) {
        try {
            mRecorder?.apply {
                stop()
                release()
                mRecorder = null
                endCount()
                if (mLocalFilePath.isEmpty()) return@apply
                onRecorderSuccess(mLocalFilePath, (endTimerCount - startTimerCount))
                logMe("success", tag)
            }
        } catch (e: Exception) {
            logMe("stopRecording ${e.localizedMessage ?: "unknown error"}")
            listener.onRecordFailure(e.localizedMessage ?: "Unknown in AudioRecordHelper")
        }
    }

    private fun initTouchListener() {
        viewToListenAt.setOnTouchListener { v, motionEvent ->
            try {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        listener.onTouchDown()
                        checkMyPermissions()
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        v.performClick()
                        listener.onTouchUp()
                        stopRecording { audioPath, duration ->
                            listener.onRecordSuccess(audioPath, duration)
                        }
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                mLocalFilePath = ""
                logMe(e.localizedMessage ?: "Unknown in AudioRecordHelper")
                listener.onRecordFailure(e.localizedMessage ?: "Unknown in AudioRecordHelper")
                true
            }

        }
    }


    private fun startCount() {
        startTimerCount = System.currentTimeMillis()
    }

    private fun endCount() {
        endTimerCount = System.currentTimeMillis()
        if (endTimerCount - startTimerCount < RECORD_MIN_DURATION) {
            throw Exception("Too short record!")
        }
    }

    private fun checkMyPermissions() {
        activityResultLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
            )
        )
    }

    private val activityResultLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val failedToGrant = permissions.entries.any { it.value == false }
            if (failedToGrant) {
                listener.onRecordFailure("Can\'t use record without permission")
                return@registerForActivityResult
            }
            startRecording()
        }

    init {
        initTouchListener()
    }

    companion object {
        const val RECORD_MIN_DURATION = 1000
    }
}

interface AudioRecordListener {
    fun onRecordSuccess(filePath: String, duration: Long)
    fun onRecordFailure(error: String)
    fun onTouchDown()
    fun onTouchUp()
}

private fun logMe(msg: String, tag: String = "TAG") {
    val showLog = IS_DEBUG
    if (!showLog) return
    Log.e(tag, msg)
}