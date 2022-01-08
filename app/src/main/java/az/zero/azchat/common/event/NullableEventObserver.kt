package az.zero.azchat.common.event

import androidx.lifecycle.Observer

/**
 * Same as [EventObserver] but support nullable types.
 */
class NullableEventObserver<T>(private val onEventUnhandledContent: (T?) -> Unit) :
    Observer<Event<T?>> {
    override fun onChanged(event: Event<T?>?) {
        event?.let { it ->
            if (!it.hasBeenHandled) {
                onEventUnhandledContent.invoke(it.getContentIfNotHandled())
            }
        }
    }
}
