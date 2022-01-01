package az.zero.azchat.common.extension

import androidx.appcompat.widget.SearchView


inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            clearFocus()
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty()) // return empty string is the text is null
            return true
        }
    })
}