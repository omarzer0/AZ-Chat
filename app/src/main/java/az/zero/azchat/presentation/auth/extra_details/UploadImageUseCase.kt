package az.zero.azchat.presentation.auth.extra_details

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import az.zero.azchat.common.RealPathUtil
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.common.logMe
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val application: Application,
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val storage: FirebaseStorage
) {

    operator fun invoke(
        uri: Uri,
        onUploadImageSuccess: (Uri) -> Unit,
        onUploadImageFailed: (String) -> Unit,
    ) {
        val realPath = RealPathUtil.getRealPath(application, uri)
        val file = Uri.fromFile(File(realPath))
        val userId = sharedPreferenceManger.uid
        val storageRef = storage.reference.child("profileImages/$userId.jpg")

        val uploadTask = try {
            val bmp = MediaStore.Images.Media.getBitmap(application.contentResolver, file)
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


}