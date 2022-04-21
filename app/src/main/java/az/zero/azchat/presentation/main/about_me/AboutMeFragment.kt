package az.zero.azchat.presentation.main.about_me

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
import az.zero.azchat.common.openLink
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentAboutMeBinding
import az.zero.azchat.presentation.main.adapter.simple_info.SimpleInfoAdapter
import dagger.hilt.android.AndroidEntryPoint


// Base Fragment
@AndroidEntryPoint
class AboutMeFragment : BaseFragment(R.layout.fragment_about_me) {

    val viewModel: AboutMeViewModel by viewModels()
    private lateinit var binding: FragmentAboutMeBinding
    private lateinit var simpleInfoAdapter: SimpleInfoAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAboutMeBinding.bind(view)
        simpleInfoAdapter = SimpleInfoAdapter(true) {
            openLink(requireContext(), it.type, it.link)
        }
        setUpRV()
        handleClicks()
        observeData()


    }

    private fun observeData() {
        viewModel.aboutMesLD.observe(viewLifecycleOwner) {
            binding.apply {
                setImageUsingGlide(ivMyImage, it.image)
                tvName.text = it.name
                tvAbout.text = it.about
                simpleInfoAdapter.changeItems(it.links)
            }
        }
    }

    private fun setUpRV() {
        binding.rvSimpleInfo.adapter = simpleInfoAdapter
    }

    private fun handleClicks() {
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
    }


}