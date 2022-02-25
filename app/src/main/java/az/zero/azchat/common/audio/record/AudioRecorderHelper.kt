package az.zero.azchat.common.audio.record

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.media.MediaRecorder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import az.zero.azchat.common.IS_DEBUG
import javax.inject.Singleton

@Singleton
class AudioRecorderHelper(
    private val context: Activity,
    private val fragment: Fragment,
    private val viewToListenAt: View,
    private val listener: AudioRecordListener
) {

    private var mRecorder: MediaRecorder? = null
    private val tag = "AudioRecorder"
    private var mLocalFilePath = ""
    private val defaultPath = "${context.externalCacheDir?.absolutePath}"
    private var relativePath = "out"
    private var fileExtension = "3gp"


    private fun startRecording(
        onRecorderFailure: (error: String) -> Unit
    ) {
        try {
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
                logMe("started", tag)
            }
        } catch (e: Exception) {
            mLocalFilePath = ""
            onRecorderFailure(e.localizedMessage ?: "Unknown")
            logMe("failed: ${e.localizedMessage}", tag)
        }
    }

    private fun stopRecording(
        onRecorderSuccess: (audioFilePath: String) -> Unit,
        onRecorderFailure: (error: String) -> Unit
    ) {
        mRecorder?.apply {
            try {
                stop()
                release()
                mRecorder = null
                if (mLocalFilePath.isEmpty()) return@apply
                onRecorderSuccess(mLocalFilePath)
                logMe("success", tag)
            } catch (e: Exception) {
                onRecorderFailure(e.localizedMessage ?: "Unknown")
                logMe("failed: ${e.localizedMessage}", tag)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTouchListener() {
        viewToListenAt.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    checkMyPermissions()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording({
                        listener.onRecordSuccess(it)
                    }, {
                        listener.onRecordFailure(it)
                    })
                    true
                }
                else -> false
            }
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
            startRecording {
                listener.onRecordFailure(it)
            }
        }

    init {
        initTouchListener()
    }
}

interface AudioRecordListener {
    fun onRecordSuccess(filePath: String)
    fun onRecordFailure(error: String)
}

private fun logMe(msg: String, tag: String = "TAG") {
    val showLog = IS_DEBUG
    if (!showLog) return
    Log.e(tag, msg)
}