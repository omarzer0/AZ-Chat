package az.zero.azchat.presentation.main.user

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.hideKeyboard
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentUserBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserFragment : BaseFragment(R.layout.fragment_user) {

    val viewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentUserBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserBinding.bind(view)
        observeEvents()
        observeData()
        setDataToViews()
        getDataFromOtherFragmentIFExists()
        handleClicks()

    }

    private fun observeData() {
        viewModel.imageMLD.observe(viewLifecycleOwner) {
            setImageUsingGlide(binding.ivUserImage, it.toString())
        }
    }

    private fun setDataToViews() {
        viewModel.user?.let {
            binding.apply {
                setImageUsingGlide(ivUserImage, it.imageUrl)
                tvUsername.text = it.name
                tvUserBio.text = it.bio
                tvUserPhone.text = it.phoneNumber
            }
        }
    }

    private fun observeEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                UserFragmentEvent.UploadingImageLoading -> {
                    binding.imagePb.show()
                }
                is UserFragmentEvent.UploadImageSuccess -> {
                    binding.imagePb.gone()
                }

                is UserFragmentEvent.UploadImageFailed -> {
                    binding.imagePb.gone()
                    toastMy("Failed to upload the image please try again later")
                }
                is UserFragmentEvent.UpdateText -> {
                    if (event.isName) {
                        binding.tvUsername.text = event.value
                    } else {
                        binding.tvUserBio.text = event.value
                    }
                }
            }
        }
    }

    private fun getDataFromOtherFragmentIFExists() {
        setFragmentResultListener(UPDATE_CODE_REQUEST) { key, bundle ->
            hideKeyboard()

            logMe(
                "$bundle:::${bundle[USER_NAME_CODE_KEY]}:::${bundle[USER_BIO_CODE_KEY]}",
                "setFragmentResultListener"
            )
            if (key == UPDATE_CODE_REQUEST) {
                viewModel.updateNameOrBio(bundle)
            } else {
                logMe("error $key", "setFragmentResultListener")
            }
        }
    }

    private fun handleClicks() {
        binding.ivEditName.setOnClickListener {
            updateNameOrAbout(true)
        }

        binding.ivEditBioAbout.setOnClickListener {
            updateNameOrAbout(false)
        }

        binding.chooseImageIv.setOnClickListener {
            checkMyPermissions()
        }
    }

    private fun updateNameOrAbout(isName: Boolean) {
        val user = viewModel.user ?: return
        navigateToAction(
            UserFragmentDirections.actionUserFragmentToUserBottomSheetFragment(
                if (isName) USER_NAME_CODE_KEY else USER_BIO_CODE_KEY,
                if (isName) user.name ?: "" else user.bio ?: "",
            )
        )
    }


    companion object {
        const val UPDATE_CODE_REQUEST = "UPDATE_CODE_REQUEST"
        const val USER_NAME_CODE_KEY = "USER_NAME_CODE_KEY"
        const val USER_BIO_CODE_KEY = "USER_BIO_CODE_KEY"
    }

    private fun checkMyPermissions() {
        activityResultLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val failedToGrant = permissions.entries.any { it.value == false }
            if (failedToGrant) {
                toastMy(getString(R.string.camera_not_granted))
                return@registerForActivityResult
            }
            pickImage { imageUrl ->
                logMe(imageUrl.toString())
                viewModel.updateUserImage(imageUrl)
            }
        }


    override fun onDestroy() {
        super.onDestroy()
        activityResultLauncher.unregister()
    }
}