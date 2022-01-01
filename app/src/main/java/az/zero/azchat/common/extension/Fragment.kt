package az.zero.azchat.common.extension


import androidx.fragment.app.Fragment
import az.zero.azchat.common.extension.hideKeyboard

fun Fragment.hideKeyboard() {
    requireActivity().hideKeyboard()
}
