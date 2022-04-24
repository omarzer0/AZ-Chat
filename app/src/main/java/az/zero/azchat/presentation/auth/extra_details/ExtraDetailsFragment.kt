package az.zero.azchat.presentation.auth.extra_details

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.extension.disable
import az.zero.azchat.common.extension.enable
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.common.tryNow
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentExtraDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExtraDetailsFragment : BaseFragment(R.layout.fragment_extra_details) {

    val viewModel: ExtraDetailsViewModel by viewModels()
    private lateinit var binding: FragmentExtraDetailsBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExtraDetailsBinding.bind(view)
        handleClicks()
        observeData()
        observeViewEvents()

    }

    private fun handleClicks() {
        binding.chooseImageIv.setOnClickListener {
            checkCameraPermissions(activityResultLauncher)
        }

        binding.doneFaBtn.setOnClickListener {
            val username = binding.usernameEt.text.toString().trim()
            val bio = binding.bioEt.text.toString().trim()
            val isChecked = binding.swHidePhoneNumber.isChecked
            viewModel.addUser(username, bio,isChecked)
        }
    }


    private fun observeData() {
        viewModel.imageMLD.observe(viewLifecycleOwner) { imageUri ->
            setImageUsingGlide(binding.userImageIv, imageUri.toString())
        }
    }

    private fun observeViewEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                is ExtraDetailsEvent.UploadImageSuccess -> {
                    binding.imagePb.gone()
                }
                is ExtraDetailsEvent.UploadImageFailed -> {
                    binding.imagePb.gone()
                    logMe(event.error)
                }
                ExtraDetailsEvent.UploadingImageLoading -> {
                    binding.imagePb.show()
                }
                is ExtraDetailsEvent.ValidateUserInputsError -> {
                    try {
                        toastMy(getString(event.error))
                    } catch (e: Exception) {
                        logMe(e.localizedMessage ?: "ValidateUserInputsError unknown error")
                    }
                }

                ExtraDetailsEvent.AddUserSuccess -> {
                    // go to main activity
                    binding.progressBarPb.progress.gone()
                    binding.doneFaBtn.enable()
                    loginInToActivity()
                }

                is ExtraDetailsEvent.AddUserError -> {
                    toastMy(event.error)
                    binding.progressBarPb.progress.gone()
                    binding.doneFaBtn.enable()
                }

                ExtraDetailsEvent.AddUserLoading -> {
                    binding.progressBarPb.progress.show()
                    binding.doneFaBtn.disable()
                }
            }
        }
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
//                setImageUsingGlide(binding.userImageIv, imageUrl.toString())
                viewModel.uploadProfileImageByUserId(imageUrl)

            }
        }

}