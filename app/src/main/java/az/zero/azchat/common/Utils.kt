package az.zero.azchat.common

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import az.zero.azchat.R
import az.zero.azchat.domain.models.simple_info.InfoTypes
import az.zero.azchat.domain.models.simple_info.InfoTypes.*
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageReference
import es.dmoral.toasty.Toasty
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

fun setImageUsingGlide(
    view: ImageView,
    image: Any?,
    isProfileImage: Boolean = true,
    errorImage: Any? = null
) {
    try {
        Glide.with(view.context)
            .load(image)
            .placeholder(getShimmerDrawable())
            .error(
                if (isProfileImage) R.drawable.no_profile_image else {
                    errorImage ?: R.drawable.ic_no_image
                }
            )
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

fun uploadImageByUserId(
    contentResolver: ContentResolver,
    realPath: String,
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
            val downloadUri = task.result ?: return@addOnCompleteListener
            logMe("success $downloadUri")
            onUploadImageSuccess(downloadUri)
        } else {
            logMe("taskUrl addOnCompleteListener failed")
            onUploadImageFailed("unknown error")
        }
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

//fun convertTimeStampToTime

fun tryNow(
    tag: String = "",
    error: ((Exception) -> Unit)? = null,
    action: () -> Unit
) {
    try {
        action()
    } catch (e: Exception) {
        logMe("error $tag ${e.localizedMessage ?: "Unknown"}", "tryNow")
    }
}

fun tryAsyncNow(
    scope: CoroutineScope,
    tag: String = "",
    error: (suspend (Exception) -> Unit)? = null,
    finally: (suspend () -> Unit)? = null,
    action: suspend () -> Unit
) {
    scope.launch {
        try {
            action()
        } catch (e: Exception) {
            error?.invoke(e)
            logMe("$tag: ${e.localizedMessage ?: "Unknown error"}", "tryAsyncNow")
        } finally {
            finally?.invoke()
        }
    }
}


fun openBrowser(context: Context, url: String?) {
    if (url.isNullOrEmpty()) return
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(browserIntent)
    } catch (e: Exception) {
    }
}

fun openFacebook(context: Context, url: String): Intent? {
    val packageManager = context.packageManager
    var uri = Uri.parse(url)
    return try {
        val applicationInfo = packageManager.getApplicationInfo("com.facebook.katana", 0)
        if (applicationInfo.enabled) {
            uri = Uri.parse("fb://facewebmodal/f?href=$url")
        }
        Intent(Intent.ACTION_VIEW, uri)
    } catch (ignored: PackageManager.NameNotFoundException) {
        openBrowser(context, url)
        null
    }
}

fun openPhone(context: Context, phone: String?) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$phone")
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
    }
}

fun openWhats(context: Context, phone: String?) {
    val url = "https://api.whatsapp.com/send?phone=$phone"
    try {
        val pm: PackageManager = context.packageManager
        pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        context.startActivity(i)
    } catch (e: PackageManager.NameNotFoundException) {
        openBrowser(context, url)
    } catch (e: Exception) {
    }
}

fun openEmail(context: Context, email: String?) {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto: $email")
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
    }
}

fun openLink(context: Context, type: Int, link: String) {
    logMe("$type => ${InfoTypes.getByValue(type)}")
    when (InfoTypes.getByValue(type)) {
        ELSE -> return
        BROWSER -> openBrowser(context, link)
        FACEBOOK -> openFacebook(context, link)
        EMAIL -> openEmail(context, link)
        PHONE -> openPhone(context, link)
        WHATS -> openWhats(context, link)
    }
}