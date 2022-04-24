package az.zero.azchat.presentation.main.image_viewer

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
import az.zero.azchat.common.FAKE_GROUP_NAME
import az.zero.azchat.common.FAKE_PROFILE_NAME
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.databinding.FragmentViewImageBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ImageViewerFragment : Fragment(R.layout.fragment_view_image) {

    private lateinit var binding: FragmentViewImageBinding

    private val viewModel: ImageViewerViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentViewImageBinding.bind(view)
//        requireActivity().showContentAboveStatusBar()

        val errorImage: Any = when (viewModel.image) {
            FAKE_PROFILE_NAME -> R.drawable.no_profile_image
            FAKE_GROUP_NAME -> R.drawable.no_group_image
            else -> R.drawable.ic_no_image
        }
        logMe(
            "chatImage: ${viewModel.image}\nerrorImage: $errorImage",
            "chatImagechatImage"
        )
        setImageUsingGlide(
            binding.photoView,
            viewModel.image,
            isProfileImage = false,
            errorImage = errorImage
        )
        binding.root.setOnClickListener { findNavController().navigateUp() }
    }


    override fun onDetach() {
        super.onDetach()
        requireActivity().window.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.white)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().window.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
    }

}