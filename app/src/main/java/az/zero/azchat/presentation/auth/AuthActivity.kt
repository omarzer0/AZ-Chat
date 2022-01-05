package az.zero.azchat.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import az.zero.azchat.R
import az.zero.azchat.core.BaseActivity
import az.zero.azchat.databinding.ActivityAuthBinding
import az.zero.azchat.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : BaseActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityAuthBinding
    private lateinit var appBarrConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        appBarrConfiguration = AppBarConfiguration(setOf(R.id.loginFragment))
        setupActionBarWithNavController(navController, appBarrConfiguration)


    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarrConfiguration) || super.onSupportNavigateUp()
    }
}