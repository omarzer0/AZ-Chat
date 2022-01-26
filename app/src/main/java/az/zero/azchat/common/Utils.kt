package az.zero.azchat.common

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import az.zero.azchat.R
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageReference
import es.dmoral.toasty.Toasty
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*


fun logMe(msg: String, tag: String = "TAG") {
    val showLog = IS_DEBUG
    if (!showLog) return
    Log.e(tag, msg)
}

fun toastMy(
    context: Context,
    message: String,
    success: Boolean = false,
    hideInRelease: Boolean = false
) {
    if (hideInRelease && !IS_DEBUG) return
    if (success) {
        Toasty.success(
            context, message, Toasty.LENGTH_SHORT, true
        ).show()
    } else {
        Toasty.error(
            context, message, Toasty.LENGTH_SHORT, true
        ).show()
    }
}

fun getShimmerDrawable(): ShimmerDrawable {
    val shimmer =
        Shimmer.AlphaHighlightBuilder()
            .setDuration(1800)
            .setBaseAlpha(0.7f)
            .setHighlightAlpha(0.6f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

    return ShimmerDrawable().apply {
        setShimmer(shimmer)
    }
}

fun setImageUsingGlide(view: ImageView, imageUrl: String?) {
    try {
        Glide.with(view.context)
            .load(imageUrl)
            .placeholder(getShimmerDrawable())
            .error(R.drawable.ic_no_image)
            .into(view)
    } catch (e: Exception) {
        logMe("setImageUsingGlide ${e.localizedMessage}")
    }
}

fun readFile(context: Context, assetFileName: String): String {
    var reader: BufferedReader? = null
    var jsonString = ""
    try {
        reader = BufferedReader(
            InputStreamReader(
                context.assets.open(assetFileName),
                "UTF-8"
            )
        )
        var line = reader.readLine()
        while (line != null) {
            jsonString += line
            line = reader.readLine()
        }

    } catch (e: Exception) {
        logMe(e.localizedMessage ?: "Unknown")
    } finally {
        reader?.let {
            try {
                reader.close()
            } catch (e: Exception) {
                logMe(e.localizedMessage ?: "Unknown")
            }
        }
    }

    return jsonString
}

val <T> T.exhaustive: T
    get() = this


fun uploadImageByUserId(
    contentResolver: ContentResolver,
    realPath:String,
    storageRef: StorageReference,
    onUploadImageSuccess: (Uri) -> Unit,
    onUploadImageFailed: (String) -> Unit,
) {
    val file = Uri.fromFile(File(realPath))

    val uploadTask = try {
        val bmp = MediaStore.Images.Media.getBitmap(contentResolver, file)
        val byteStreamArray = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, byteStreamArray)
        val data: ByteArray = byteStreamArray.toByteArray()
        storageRef.putBytes(data)
    } catch (e: Exception) {
        onUploadImageFailed("Failed to upload the image Please try again!")
        return
    }

    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let {
                logMe(it.localizedMessage ?: "taskUrl unknown error")
                onUploadImageFailed(it.localizedMessage ?: "unknown error")
            }
        }
        storageRef.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUri = task.result
            logMe("success $downloadUri")
            onUploadImageSuccess(downloadUri)
        } else {
            logMe("taskUrl addOnCompleteListener failed")
            onUploadImageFailed("unknown error")
        }
    }
}

fun pickImage(view: View, action: (Uri) -> Unit) {
    TedImagePicker.with(view.context)
        .title("Choose image")
        .backButton(R.drawable.ic_arrow_back_black_24dp)
        .showCameraTile(true)
        .buttonBackground(R.drawable.btn_done_button)
        .buttonTextColor(R.color.white)
        .buttonText("Choose image")
        .errorListener { throwable -> logMe(throwable.localizedMessage ?: "pickImage") }
        .start { uri ->
            action(uri)
        }
}

fun convertTimeStampToDate(timestamp: Timestamp): String = try {
    val language = Locale.getDefault().displayLanguage
    val sfd = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale(language))
    sfd.format(Date(timestamp.seconds * 1000))
} catch (e: Exception) {
    logMe(e.localizedMessage ?: "convertTimeStampToDate unknown")
    ""
}

fun tryNow(
    error: ((Exception) -> Unit)? = null,
    action: () -> Unit
) {
    try {
        action()
    } catch (e: Exception) {
        logMe(e.localizedMessage ?: "Unknown", "tryNow")
    }
}

fun tryAsyncNow(
    scope: CoroutineScope,
    error: (suspend (Exception) -> Unit)? = null,
    finally: (() -> Unit)? = null,
    action: suspend () -> Unit
) {
    scope.launch {
        try {
            action()
        } catch (e: Exception) {
            error?.invoke(e)
            logMe(e.localizedMessage ?: "Unknown", "tryAsyncNow")
        } finally {
            finally?.invoke()
        }
    }
}


//inline fun <reified T> DocumentSnapshot.toValidObject(): T? {
//    return try {
//        this.toObject<T>()
//    } catch (e: Exception) {
//        null
//    }
//}

////
////    private val _status = MutableLiveData<Event<Status>>()
////    val status: LiveData<Event<Status>> = _status
////
////    private fun <QuerySnapshot> safeCallApi(
////        action: suspend () -> Task<QuerySnapshot>,
////        response: (QuerySnapshot) -> Unit,
////        messageIfSuccess: String? = null,
////        showLoadingBar: Boolean = true
////    ) {
////        viewModelScope.launch(exceptionHandler) {
////            if (showLoadingBar) {
////                _status.value = Event(Status.Loading)
////            }
////            val callResponse = action()
////
////            callResponse.addOnSuccessListener {
////                response(it)
////                _status.value = Event(Status.Success(messageIfSuccess))
////            }.addOnFailureListener {
////                _status.value = Event(Status.Error(it.localizedMessage ?: "Unknown"))
////            }
////        }
////    }
////
////    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
////        viewModelScope.launch(Dispatchers.Main) {
////
////
////            val throwableMessage = throwable.localizedMessage
////            val errorMessage =
////                if (throwableMessage != null && throwableMessage.contains("No address associated with hostname"))
////                    "Check your internet connection!"
////                else "error"
////
////            _status.value =
////                Event(Status.Error(errorMessage))
////        }
////    }
////}
////
////sealed class Result<T>(
////    val message: String? = null,
////    val data: T? = null
////) {
////    class Success<T>(message: String? = null, data: T? = null) : Result<T>(message, data)
////    class Loading<T>(message: String? = null, data: T? = null) : Result<T>(message, data)
////    class Error<T>(message: String? = null, data: T? = null) : Result<T>(message, data)
////}
////
////sealed class Status {
////    data class Success(val message: String?) : Status()
////    data class Error(val message: String?) : Status()
////    object Loading : Status()
////    object Empty : Status()
////}
////