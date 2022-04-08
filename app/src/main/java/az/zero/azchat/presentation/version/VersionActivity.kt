package az.zero.azchat.presentation.version

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import az.zero.azchat.R
import az.zero.azchat.common.APP_VERSION_CODE

class VersionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version)

        findViewById<TextView>(R.id.tv_version_text).text =
            "${getString(R.string.this_is_an_old_version_please_update)}\n V=$APP_VERSION_CODE"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}