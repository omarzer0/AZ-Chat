package az.zero.azchat.presentation.auth

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.core.BaseActivity
import az.zero.azchat.databinding.ActivityAuthBinding
import az.zero.azchat.presentation.version.VersionChecker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : BaseActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityAuthBinding
    private lateinit var appBarrConfiguration: AppBarConfiguration
    lateinit var graph: NavGraph

    @Inject
    lateinit var versionChecker: VersionChecker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.auth_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        appBarrConfiguration = AppBarConfiguration(setOf(R.id.loginFragment))
        setupActionBarWithNavController(navController, appBarrConfiguration)

        val inflater = navHostFragment.navController.navInflater
        graph = inflater.inflate(R.navigation.auth_nav_graph)

        if (sharedPreferences.openedTheAppBefore) {
            graph.setStartDestination(R.id.loginFragment)
        } else {
            graph.setStartDestination(R.id.onBoardingFragment)
        }

        navHostFragment.navController.graph = graph
        observeDestinations()
    }

    private fun observeDestinations() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.onBoardingFragment) hideAppBar()
            else showAppBar()
        }
    }

    override fun onBackPressed() {
        navController.currentDestination?.let {
            if (it.id == R.id.loginFragment) {
                finish()
            } else {
                super.onBackPressed()
            }
        } ?: super.onBackPressed()
    }

    private fun showAppBar() {
        binding.toolbar.show()
    }

    private fun hideAppBar() {
        binding.toolbar.gone()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarrConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        versionChecker()
    }
}