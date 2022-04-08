package az.zero.azchat.common.extension

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import az.zero.azchat.R
import az.zero.azchat.common.logMe


fun Activity.hideKeyboard() {
    val inputMethodManager: InputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Find the currently focused view, so we can grab the correct window token from it.
    var view: View? = currentFocus
    // If no view currently has focus, create a new one, just so we can grab a window token from it.
    if (view == null) {
        view = View(this)
    }
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.showKeyboard() {
    val inputMethodManager: InputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Find the currently focused view, so we can grab the correct window token from it.
    var view: View? = currentFocus
    // If no view currently has focus, create a new one, just so we can grab a window token from it.
    if (view == null) {
        view = View(this)
    }
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.makeStatusBarTransparent() {
    window.apply {
        logMe(
            "makeStatusBarTransparent ${decorView.systemUiVisibility}",
            "makeStatusBarTransparent"
        )
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        statusBarColor = Color.TRANSPARENT
    }
}

fun Activity.clearTransparentStatusBar() {
    window.apply {
        decorView.systemUiVisibility = View.STATUS_BAR_VISIBLE
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor =
            ContextCompat.getColor(this@clearTransparentStatusBar, R.color.secondaryColor)
    }
}

fun View.setMargin(
    marginLeft: Int = 0,
    marginTop: Int = 0,
    marginRight: Int = 0,
    marginBottom: Int = 0
) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom)
    this.layoutParams = menuLayoutParams
}