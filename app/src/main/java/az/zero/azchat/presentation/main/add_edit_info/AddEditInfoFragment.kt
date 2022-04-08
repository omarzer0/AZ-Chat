package az.zero.azchat.presentation.main.add_edit_info

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentAddEditInfoBinding
import az.zero.azchat.domain.models.private_chat.PrivateChat
import az.zero.azchat.domain.models.user.User
import az.zero.azchat.presentation.auth.extra_details.ExtraDetailsEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditInfoFragment : BaseFragment(R.layout.fragment_add_edit_info) {

    val viewModel: AddEditInfoViewModel by viewModels()
    private lateinit var binding: FragmentAddEditInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddEditInfoBinding.bind(view)
        handleClicks()
        observeData()
        observeEvents()

    }

    private fun observeEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                UploadingImageLoading -> {
                    binding.imagePb.show()
                }
                is UploadImageSuccess -> {
                    binding.imagePb.gone()
                }

                is UploadImageFailed -> {
                    binding.imagePb.gone()
                    toastMy("Failed to upload the image please try again later")
                }

            }
        }
    }

    private fun observeData() {
        viewModel.imageMLD.observe(viewLifecycleOwner) { imageUri ->
            setImageUsingGlide(binding.groupImageIv, imageUri.toString())
        }
    }

    private fun handleClicks() {
        binding.doneFab.setOnClickListener {
            val groupName = binding.groupNameEt.text?.trim()?.toString() ?: ""
            val aboutGroup = binding.groupAboutEt.text?.trim()?.toString() ?: ""
            if (groupName.isEmpty() || groupName.length < 3) {
                toastMy("Please enter 3 characters or more for group name")
                return@setOnClickListener
            }

            viewModel.addNewGroup(groupName,aboutGroup) { newGroup ->
                navigateToAction(
                    AddEditInfoFragmentDirections.actionAddEditInfoFragmentToPrivateChatRoomFragment(
                        PrivateChat(newGroup, User(), newGroup.gid!!),
                        false
                    )
                )
            }
        }

        binding.chooseImageIv.setOnClickListener {
            checkMyPermissions()
        }
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
                viewModel.uploadGroupImage(imageUrl)

            }
        }


}