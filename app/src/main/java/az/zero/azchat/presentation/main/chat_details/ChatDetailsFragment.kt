package az.zero.azchat.presentation.main.chat_details

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import az.zero.azchat.MainNavGraphDirections
import az.zero.azchat.R
import az.zero.azchat.common.FAKE_GROUP_NAME
import az.zero.azchat.common.FAKE_PROFILE_NAME
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.hideKeyboard
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentChatDetailsBinding
import az.zero.azchat.presentation.main.adapter.user.UserAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatDetailsFragment : BaseFragment(R.layout.fragment_chat_details) {

    val viewModel: ChatDetailsViewModel by viewModels()
    private lateinit var binding: FragmentChatDetailsBinding
    private val userAdapter =
        UserAdapter(onImageClick = { image ->
            val action = MainNavGraphDirections.actionGlobalImageViewerFragment(image)
            navigateToAction(action)
        }, onUserChosenToJoinGroup = {}, onUserClickListener = {})

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatDetailsBinding.bind(view)
        setDataToViews()
        setUpRV()
        handleClicks()
        observeData()
        observeEvents()
        getDataFromOtherFragmentIFExists()
    }

    private fun setUpRV() {
        binding.rvGroupUsers.adapter = userAdapter
    }

    private fun observeEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                ChatDetailsEvent.UploadingImageLoading -> {
                    binding.imagePb.show()
                }
                is ChatDetailsEvent.UploadImageSuccess -> {
                    binding.imagePb.gone()
                }

                is ChatDetailsEvent.UploadImageFailed -> {
                    binding.imagePb.gone()
                    toastMy("Failed to upload the image please try again later")
                }
                is ChatDetailsEvent.UpdateText -> {
                    viewModel.haveUpdate = true
                    if (event.isName) {
                        binding.tvChatName.text = event.value
                    } else {
                        binding.tvChatBio.text = event.value
                    }
                }
            }
        }
    }

    private fun observeData() {
        viewModel.imageMLD.observe(viewLifecycleOwner) {
            setImageUsingGlide(binding.ivChatImage, it.toString())
        }

        viewModel.usersMLD.observe(viewLifecycleOwner) { users ->
            if (users.isEmpty()) {
                binding.clMembersRootView.gone()
                return@observe
            }
            userAdapter.submitList(users)
            binding.clMembersRootView.show()
        }

    }

    private fun setDataToViews() {
        val privateChat = viewModel.getCurrentPrivateChat()
        val group = privateChat.group
        val user = privateChat.user
        val isGroup = group.ofTypeGroup!!
        showCorrectViews(isGroup)
        val name = if (isGroup) group.name!! else user.name!!
        val image = if (isGroup) group.image!! else user.imageUrl!!
        var about = if (isGroup) group.about!! else user.bio!!
        if (about.trim().isEmpty()) {
            about = if (isGroup) getString(R.string.lazy_users)
            else getString(R.string.lazy_user)
        }

        binding.apply {
            setImageUsingGlide(
                ivChatImage,
                image,
                isProfileImage = false,
                if (isGroup) R.drawable.no_group_image else R.drawable.no_profile_image
            )
            tvChatName.text = name
            tvChatBio.text = about

            if (isGroup) {
                aboutTv.text = getString(R.string.about)
                tvChatNumberOfMembers.text =
                    "${group.members!!.size} ${getString(R.string.members)}"
            } else {
                aboutTv.text = getString(R.string.bio)
                tvChatPhone.text = user.phoneNumber ?: ""
            }
        }
    }

    private fun showCorrectViews(isGroup: Boolean) {
        binding.apply {
            chooseImageIv.isVisible = isGroup
            ivEditName.isVisible = isGroup
            ivEditBioAbout.isVisible = isGroup
            tvChatNumberOfMembers.isVisible = isGroup
            tvChatPhone.isVisible = !isGroup
        }
    }

    private fun handleClicks() {
        binding.apply {
            chooseImageIv.setOnClickListener { checkMyPermissions() }

            ivEditName.setOnClickListener {
                updateNameOrAbout(true)
            }

            ivEditBioAbout.setOnClickListener {
                updateNameOrAbout(false)
            }

            ivChatImage.setOnClickListener {
                val group = viewModel.getCurrentPrivateChat().group
                val user = viewModel.getCurrentPrivateChat().user
                val isGroup = viewModel.getCurrentPrivateChat().group.ofTypeGroup ?: false
                val chatImage = if (isGroup) group.image ?: "" else user.imageUrl ?: ""
                val image = chatImage.ifEmpty {
                    if (isGroup) FAKE_GROUP_NAME else FAKE_PROFILE_NAME
                }
                val action = MainNavGraphDirections.actionGlobalImageViewerFragment(image)
                navigateToAction(action)
            }
        }
    }

    private fun updateNameOrAbout(isName: Boolean) {
        val group = viewModel.getCurrentPrivateChat().group
        val user = viewModel.getCurrentPrivateChat().user
        val isGroup = group.ofTypeGroup!!
        val name = if (isGroup) group.name!! else user.name!!
        val about = if (isGroup) group.about!! else user.bio!!
        navigateToAction(
            ChatDetailsFragmentDirections.actionChatDetailsFragmentToChatDetailsBottomSheetFragment(
                if (isName) NAME_CODE_KEY else ABOUT_CODE_KEY,
                if (isName) name else about
            )
        )
    }

    private fun getDataFromOtherFragmentIFExists() {
        setFragmentResultListener(UPDATE_CODE_REQUEST) { key, bundle ->
            hideKeyboard()

            logMe(
                "$bundle:::${bundle[NAME_CODE_KEY]}:::${bundle[ABOUT_CODE_KEY]}",
                "setFragmentResultListener"
            )
            if (key == UPDATE_CODE_REQUEST) {
                viewModel.updateNameOrAbout(bundle)
            } else {
                logMe("error $key", "setFragmentResultListener")
            }
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
                viewModel.updateChatRoomImage(imageUrl)
            }
        }

    companion object {
        const val UPDATE_CODE_REQUEST = "UPDATE_CODE_REQUEST"
        const val NAME_CODE_KEY = "NAME_CODE_KEY"
        const val ABOUT_CODE_KEY = "ABOUT_CODE_KEY"

    }

    override fun onDestroyView() {
        super.onDestroyView()
        activityResultLauncher.unregister()
    }

}