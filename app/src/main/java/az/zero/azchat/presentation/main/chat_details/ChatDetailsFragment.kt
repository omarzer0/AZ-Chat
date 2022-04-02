package az.zero.azchat.presentation.main.chat_details

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.hideKeyboard
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentChatDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatDetailsFragment : BaseFragment(R.layout.fragment_chat_details) {

    val viewModel: ChatDetailsViewModel by viewModels()
    private lateinit var binding: FragmentChatDetailsBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatDetailsBinding.bind(view)
        setDataToViews()
        handleClicks()
        observeData()
        observeEvents()
        getDataFromOtherFragmentIFExists()

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
    }

    private fun setDataToViews() {
        val privateChat = viewModel.getCurrentPrivateChat()
        val group = privateChat.group
        val user = privateChat.user
        val isGroup = group.ofTypeGroup!!
        showCorrectViews(isGroup)
        val name = if (isGroup) group.name!! else user.name!!
        val image = if (isGroup) group.image!! else user.imageUrl!!
        val about = if (isGroup) group.about!! else user.bio!!

        binding.apply {
            setImageUsingGlide(ivChatImage, image)
            tvChatName.text = name
            tvChatBio.text = about
            if (isGroup) tvChatNumberOfMembers.text =
                "${group.members!!.size} ${getString(R.string.members)}"
            else tvChatPhone.text = user.phoneNumber ?: ""
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

    override fun onDestroy() {
        super.onDestroy()
        activityResultLauncher.unregister()
    }
}