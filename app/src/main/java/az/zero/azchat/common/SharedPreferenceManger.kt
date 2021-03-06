package az.zero.azchat.common

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceManger @Inject constructor(
    @ApplicationContext context: Context
) {

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    var authToken: String
        get() = getStringValue(AUTH_TOKEN) ?: ""
        set(value) = setValue(AUTH_TOKEN, value)

    var phoneNumber: String
        get() = getStringValue(PHONE_NUMBER) ?: ""
        set(value) = setValue(PHONE_NUMBER, value)

    var uid: String
        get() = getStringValue(UID) ?: ""
        set(value) = setValue(UID, value)

    var hasLoggedIn: Boolean
        get() = getBooleanValue(LOGGED_IN)
        set(value) = setValue(LOGGED_IN, value)

    var openedTheAppBefore: Boolean
        get() = getBooleanValue(OPENED_THE_APP_BEFORE)
        set(value) = setValue(OPENED_THE_APP_BEFORE, value)

    var notificationToken: String
        get() = getStringValue(NOTIFICATION_TOKEN) ?: ""
        set(value) = setValue(NOTIFICATION_TOKEN, value)

    var userName: String
        get() = getStringValue(USER_NAME) ?: ""
        set(value) = setValue(USER_NAME, value)

    var userImage: String
        get() = getStringValue(USER_IMAGE) ?: ""
        set(value) = setValue(USER_IMAGE, value)

    var currentGid: String
        get() = getStringValue(CURRENT_GID) ?: ""
        set(value) = setValue(CURRENT_GID, value)

//    var isNotificationsEnabled: Boolean
//        get() = getBooleanValue(IS_NOTIFICATIONS_ENABLED, true)
//        set(value) = setValue(IS_NOTIFICATIONS_ENABLED, value)

    // this is bad but the alt is to use a database for a single list
    var blockList: List<String>
        get() = getUserBlockList()
        set(value) = setUserBlockList(value)

    private fun getUserBlockList(): List<String> {
        val value = getStringValue(USER_BLOCK_LIST)
        return if (value == EMPTY) {
            emptyList()
        } else {
            gson.fromJson(value, Array<String>::class.java).toList()
        }
    }

    private fun setUserBlockList(value: List<String>) {
        val json = gson.toJson(value)
        setValue(USER_BLOCK_LIST, json)
    }

    fun setValue(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun setValue(key: String, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    fun setValue(key: String, value: Float) {
        editor.putFloat(key, value)
        editor.apply()
    }

    fun setValue(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getStringValue(key: String): String? {
        return sharedPreferences.getString(key, EMPTY)
    }

    fun getIntegerValue(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun getFloatValue(key: String): Float {
        return sharedPreferences.getFloat(key, 0F)
    }

    fun getBooleanValue(key: String, default: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun remove(key: String) {
        editor.remove(key)
        editor.apply()
    }

    fun nuke() {
        sharedPreferences.edit().clear().apply()
        hasLoggedIn = false
        uid = ""
        authToken = ""
        phoneNumber = ""
        currentGid = ""
        userImage = ""
        userName = ""
        notificationToken = ""
        blockList = emptyList()
        openedTheAppBefore = true
    }

    companion object {
        const val SHARED_PREFERENCES_NAME = "login shared pref"
        const val EMPTY = ""
        const val AUTH_TOKEN = "AUTH_TOKEN"
        const val UID = "uid"
        const val LOGGED_IN = "logged_in"
        const val OPENED_THE_APP_BEFORE = "opened the app before"
        const val PHONE_NUMBER = "phone number"
        const val NOTIFICATION_TOKEN = "Notification token"
        const val USER_NAME = "current username"
        const val USER_IMAGE = "UserImage"
        const val IS_NOTIFICATIONS_ENABLED = "is notifications enabled"
        const val CURRENT_GID = "CURRENT_GID"
        const val USER_BLOCK_LIST = "USER_BLOCK_LIST"
    }
}