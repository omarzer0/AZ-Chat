package az.zero.azchat.core

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.presentation.auth.AuthActivity
import az.zero.azchat.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var sharedPreferences: SharedPreferenceManger


    fun loginInToActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        sharedPreferences.hasLoggedIn = true
        finish()
    }

    fun loginOutFromActivity() {
        startActivity(Intent(this, AuthActivity::class.java))
        sharedPreferences.apply {
            hasLoggedIn = false
            authToken = ""
            phoneNumber = ""
            uid = ""
            userName = ""
            openedTheAppBefore = false
        }
        finish()
    }
}