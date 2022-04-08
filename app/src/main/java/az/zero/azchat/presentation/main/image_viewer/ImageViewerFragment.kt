package az.zero.azchat.presentation.main.image_viewer

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
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
        changeStatusBarColor(true)

        setImageUsingGlide(binding.photoView, viewModel.image)
        binding.root.setOnClickListener { findNavController().navigateUp() }
    }

    private fun changeStatusBarColor(isEntering: Boolean) {
        val window: Window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor =
            if (isEntering) Color.BLACK else ContextCompat.getColor(
                requireContext(),
                R.color.secondaryColor
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        changeStatusBarColor(false)
    }

}