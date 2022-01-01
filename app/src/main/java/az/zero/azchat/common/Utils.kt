package az.zero.azchat.common

import android.content.Context
import android.util.Log
import android.widget.ImageView
import az.zero.azchat.R
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import es.dmoral.toasty.Toasty
import java.io.BufferedReader
import java.io.InputStreamReader


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

//fun setUpSearchView(searchBar: SearchBarBinding, actionWhenSearch: (searchString: String) -> Unit) {
//    searchBar.apply {
//        searchEt.doOnTextChanged { text, _, _, _ ->
//            cancelIconIv.isVisible = !text.isNullOrBlank()
//        }
//
//        cancelIconIv.setOnClickListener {
//            searchEt.setText("")
//        }
//
//        searchEt.setOnEditorActionListener { textView, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                actionWhenSearch(textView.text.toString())
//                textView.context.hideKeyboard(textView)
//                true
//            } else false
//        }
//    }
//}
