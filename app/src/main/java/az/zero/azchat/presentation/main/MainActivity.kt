package az.zero.azchat.presentation.main

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import az.zero.azchat.MainNavGraphDirections
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.extension.showContentAboveStatusBar
import az.zero.azchat.common.extension.showContentNormallyUnderStatusBarWithMainColor
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.common.toastMy
import az.zero.azchat.core.BaseActivity
import az.zero.azchat.databinding.ActivityMainBinding
import az.zero.azchat.presentation.version.VersionChecker
import com.google.android.material.imageview.ShapeableImageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding
    private lateinit var appBarrConfiguration: AppBarConfiguration
    val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var versionChecker: VersionChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!sharedPreferences.hasLoggedIn) loginOutFromActivity()
        logMe("hasLoggedIn ${sharedPreferences.hasLoggedIn}", "hasLoggedIn")

        setSupportActionBar(binding.toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        appBarrConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.imageViewerFragment),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarrConfiguration)
        binding.navDrawerSlider.setupWithNavController(navController)

        handleClicks()
        observeData()
        observeDestinations()
    }

    private fun observeDestinations() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.privateChatRoomFragment -> {
                    showChatAppBar()
                    showContentNormallyUnderStatusBarWithMainColor()
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                }
                R.id.homeFragment -> {
                    hideChatAppBar()
                    showContentNormallyUnderStatusBarWithMainColor()
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }

                R.id.chatDetailsFragment, R.id.chatDetailsBottomSheetFragment, R.id.userFragment, R.id.userBottomSheetFragment -> {
                    hideMainAppBar()
                    showContentAboveStatusBar()
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

                R.id.aboutMeFragment -> {
                    hideMainAppBar()
                    showContentNormallyUnderStatusBarWithMainColor()
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

                R.id.addChatFragment -> {
                    hideChatAppBar()
                    showContentNormallyUnderStatusBarWithMainColor()
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

                R.id.imageViewerFragment -> {
                    hideChatAppBar()
                    hideMainAppBar()
                    showContentAboveStatusBar(Color.TRANSPARENT)
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                else -> {
                    showChatAppBar()
                    showContentNormallyUnderStatusBarWithMainColor()
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
            }
        }
    }

    private fun showMainAppBar() {
        binding.toolbar.show()
    }

    private fun hideMainAppBar() {
        binding.toolbar.gone()
    }

    private fun showChatAppBar() {
        binding.chatCl.show()
        showMainAppBar()
    }

    private fun hideChatAppBar() {
        binding.chatCl.gone()
        showMainAppBar()
    }


    private fun observeData() {
        val header = binding.navDrawerSlider.getHeaderView(0)
        header.setOnClickListener { goToProfile() }

        val userImageIV = header.findViewById<ShapeableImageView>(R.id.header_user_image_iv)
        val userNameTV = header.findViewById<TextView>(R.id.username_tv)
        val userPhoneNumberTV = header.findViewById<TextView>(R.id.phone_number_tv)
        val logoutIV = header.findViewById<ImageView>(R.id.logout_iv)

        logoutIV.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.sure_want_to_logout))
                .setPositiveButton(getString(R.string.logout)) { dialog, _ ->
                    viewModel.logOut()
                    dialog.dismiss()
                    loginOutFromActivity()
                }.setNegativeButton(getString(R.string.stay)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        viewModel.user.observe(this) {
            userNameTV.text = it.name
            userPhoneNumberTV.text = it.phoneNumber
            it.phoneNumber?.let { number -> sharedPreferences.phoneNumber = number }
            it.name?.let { name -> sharedPreferences.userName = name }
            it.imageUrl?.let { image -> sharedPreferences.userImage = image }
            setImageUsingGlide(userImageIV, it.imageUrl)
            sharedPreferences.blockList = it.blockList
        }
    }


    private fun handleClicks() {

        binding.navDrawerSlider.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.header_user_image_iv -> {
                    logMe("clicked")
                }

                R.id.go_to_profile -> {
                    goToProfile()
                }

                R.id.go_to_about_developer -> {
                    navController.navigate(MainNavGraphDirections.actionGlobalAboutMeFragment())
//                    toastMy(this, "Wanna know about me? not now hahah", true)
                }

                R.id.go_to_licence -> {
                    toastMy(this, "Will be added on the release")
                }
            }

            menuItem.isChecked = true
            binding.drawerLayout.close()
            true
        }
    }

    private fun goToProfile() {
        viewModel.getUser()?.let {
            val action = MainNavGraphDirections.actionGlobalUserFragment(it)
            navController.navigate(action)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarrConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        versionChecker()
    }
}