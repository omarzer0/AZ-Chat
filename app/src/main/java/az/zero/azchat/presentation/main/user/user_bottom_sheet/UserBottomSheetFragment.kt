package az.zero.azchat.presentation.main.user.user_bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
import az.zero.azchat.common.toastMy
import az.zero.azchat.databinding.BottomSheetFragmentUserBinding
import az.zero.azchat.presentation.main.user.UserFragment.Companion.UPDATE_CODE_REQUEST
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserBottomSheetFragment : BottomSheetDialogFragment() {

    val viewModel: UserBottomSheetViewModel by viewModels()
    private lateinit var binding: BottomSheetFragmentUserBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_fragment_user, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = BottomSheetFragmentUserBinding.bind(view)
        setDataToViews()
        handleClicks()

    }

    private fun setDataToViews() {
        binding.edText.setText(viewModel.text)
    }

    private fun handleClicks() {
        binding.apply {
            btnUpdate.setOnClickListener {
                val text = edText.text.toString()
                if (text.isEmpty()) {
                    toastMy(
                        requireContext(),
                        "Please enter 3 or more characters",
                        false
                    )
                    return@setOnClickListener
                }
                setFragmentResult(UPDATE_CODE_REQUEST, bundleOf(viewModel.code to text))
                findNavController().navigateUp()
            }
        }
    }


}