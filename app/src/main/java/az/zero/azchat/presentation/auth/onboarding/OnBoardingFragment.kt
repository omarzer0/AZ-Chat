package az.zero.azchat.presentation.auth.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.common.extension.hide
import az.zero.azchat.common.extension.show
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentOnboardingBinding
import az.zero.azchat.domain.models.slider.SliderItem
import az.zero.azchat.presentation.auth.adapter.slider.SliderAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingFragment : BaseFragment(R.layout.fragment_onboarding) {

    private lateinit var binding: FragmentOnboardingBinding
    private val sliderAdapter = SliderAdapter()

    @Inject
    lateinit var sharedPreferenceManger: SharedPreferenceManger

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnboardingBinding.bind(view)
        handleClicks()
        setUpSlider()
    }

    private fun setUpSlider() {
        val items = mutableListOf<SliderItem>().apply {
            add(
                SliderItem(
                    R.raw.fast_to_send,
                    "Fast",
                    "AZ chat delivers messages faster than other applications."
                )
            )
            add(SliderItem(R.raw.safe, "Secure", "AZ chat keeps your messages secure and safe"))
            add(
                SliderItem(
                    R.raw.free_to_chat, "Free", "AZ chat is a free to use. No ads or subscription fees"
                )
            )
        }

        sliderAdapter.changeItems(items)
        binding.viewPager2.adapter = sliderAdapter

        TabLayoutMediator(binding.tlTabDots, binding.viewPager2) { tab, position ->

        }.attach()

        binding.tlTabDots.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab == null) return
                if (tab.position == items.size - 1) binding.btnStart.show()
                else binding.btnStart.hide()

//                Log.e("TAG", "setUpSlider: $tab.position ${items.size - 1}")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun handleClicks() {
        binding.btnStart.setOnClickListener {
            sharedPreferenceManger.openedTheAppBefore = true
//
//            val startDestination = R.id.loginFragment
//            val navOptions = NavOptions.Builder()
//                .setPopUpTo(startDestination, true)
//                .build()
//            findNavController().navigate(startDestination, null, navOptions)

            navigateToAction(OnBoardingFragmentDirections.actionOnBoardingFragmentToLoginFragment())
        }
    }


}