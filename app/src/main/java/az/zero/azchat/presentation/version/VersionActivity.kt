package az.zero.azchat.presentation.version

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import az.zero.azchat.R
import az.zero.azchat.common.logMe
import az.zero.azchat.common.openBrowser
import az.zero.azchat.common.openLink
import az.zero.azchat.databinding.ActivityVersionBinding
import az.zero.azchat.domain.models.versions.Versions
import az.zero.azchat.presentation.main.adapter.simple_info.SimpleInfoAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VersionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVersionBinding
    val viewModel: VersionViewModel by viewModels()

    private var versions = Versions()
    private lateinit var simpleInfoAdapter: SimpleInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVersionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        simpleInfoAdapter = SimpleInfoAdapter {
            openLink(this, it.type, it.link)
        }
        showOrHideViews()
        setDataToViews()
        observeData()
        setUpRV()
        handleClicks()
    }

    private fun setUpRV() {
        binding.rvSimpleInfo.adapter = simpleInfoAdapter
    }

    private fun setDataToViews() {
        binding.tvVersionText.text = getString(R.string.this_is_an_old_version_please_update)
    }

    private fun observeData() {
        viewModel.versionsLD.observe(this) {
            versions = it
            logMe(it.links.toString(), "changeItems")
            simpleInfoAdapter.changeItems(it.links)
            showOrHideViews()
        }
    }

    private fun showOrHideViews() {
        binding.apply {
            tvVersionHeader.apply {
                isVisible = versions.header.isNotEmpty()
                text = versions.header
            }

            tvNote.apply {
                isVisible = versions.note.isNotEmpty()
                text = versions.note
            }

            rvSimpleInfo.apply {
                isVisible = versions.links.isNotEmpty()
            }

            tvLinksHeader.apply {
                isVisible = versions.links.isNotEmpty()
                text = versions.linksHeader
            }

            tvGetTheNewVersion.isVisible = versions.newVersionLink.isNotEmpty()
        }
    }

    private fun handleClicks() {
        binding.tvGetTheNewVersion.setOnClickListener {
            openBrowser(this, versions.newVersionLink)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}