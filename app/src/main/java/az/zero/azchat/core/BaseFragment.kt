package az.zero.azchat.core

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
import az.zero.azchat.common.IS_DEBUG
import az.zero.azchat.common.event.Event
import az.zero.azchat.common.logMe
import az.zero.azchat.common.tryNow
import es.dmoral.toasty.Toasty
import gun0912.tedimagepicker.builder.TedImagePicker

abstract class BaseFragment(layout: Int) : Fragment(layout) {

    fun pickImage(action: (Uri) -> Unit) {
        TedImagePicker.with(requireContext())
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

    fun toastMy(
        message: String,
        success: Boolean = false,
        hideInRelease: Boolean = false
    ) {
        if (hideInRelease && !IS_DEBUG) return
        if (success) {
            Toasty.success(
                requireContext(), message, Toasty.LENGTH_SHORT, true
            ).show()
        } else {
            Toasty.error(
                requireContext(), message, Toasty.LENGTH_SHORT, true
            ).show()
        }
    }


    fun navigateToAction(
        action: NavDirections,
        shouldHaveNoAnimation: Boolean = false,
        navOptions: NavOptions = NavOptions.Builder()
            .setEnterAnim(android.R.anim.fade_in)
            .setPopEnterAnim(android.R.anim.fade_in)
            .build()
    ) {
        tryNow {
            if (shouldHaveNoAnimation) findNavController().navigate(action)
            else findNavController().navigate(action, navOptions)
        }
    }

    protected fun <T> LiveData<Event<T>>.observeIfNotHandled(result: (T) -> Unit) {
        this.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                result(it)
            }
        }
    }

    protected fun <T> MutableLiveData<Event<T>>.observeIfNotHandled(result: (T) -> Unit) {
        this.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                result(it)
            }
        }
    }

    fun loginInToActivity() {
        if (requireActivity() is BaseActivity) {
            (requireActivity() as BaseActivity).loginInToActivity()
        }
    }

    fun loginOutFromActivity() {
        if (requireActivity() is BaseActivity) {
            (requireActivity() as BaseActivity).loginOutFromActivity()
        }
    }
}