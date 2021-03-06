package az.zero.azchat.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import az.zero.azchat.core.BaseActivity
import az.zero.azchat.presentation.auth.AuthActivity
import az.zero.azchat.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (sharedPreferences.hasLoggedIn)
            startActivity(Intent(this, MainActivity::class.java))
        else startActivity(Intent(this, AuthActivity::class.java))
    }
}