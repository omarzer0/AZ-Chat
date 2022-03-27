package az.zero.azchat.presentation.main

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
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.core.BaseActivity
import az.zero.azchat.databinding.ActivityMainBinding
import com.google.android.material.imageview.ShapeableImageView

class MainActivity : BaseActivity() {

    private lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding
    private lateinit var appBarrConfiguration: AppBarConfiguration
    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        appBarrConfiguration = AppBarConfiguration(setOf(R.id.homeFragment), binding.drawerLayout)

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
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                }
                R.id.homeFragment -> {
                    hideChatAppBar()
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }

                R.id.chatDetailsFragment, R.id.chatDetailsBottomSheetFragment -> {
                    hideMainAppBar()
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

                else -> {
                    showChatAppBar()
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
        }
    }

    private fun handleClicks() {

        binding.navDrawerSlider.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.header_user_image_iv -> {
                    logMe("clicked")
                }
            }

            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarrConfiguration) || super.onSupportNavigateUp()
    }
}