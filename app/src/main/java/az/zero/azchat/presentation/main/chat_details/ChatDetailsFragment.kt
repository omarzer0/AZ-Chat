package az.zero.azchat.presentation.main.chat_details

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import az.zero.azchat.R
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

    }


}