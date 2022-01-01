package az.zero.azchat.core

import androidx.appcompat.app.AppCompatActivity
import az.zero.azchat.common.SharedPreferenceManger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var sharedPreferences: SharedPreferenceManger
}