package com.nlhd.appperformance.Activity

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.nlhd.appperformance.Fragment.SearchFragmentDirections
import com.nlhd.appperformance.R
import com.nlhd.appperformance.ViewModel.SearchSuccessViewModel
import com.nlhd.appperformance.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySearchBinding

    private val viewModel: SearchSuccessViewModel by viewModels()

    @Inject
    lateinit var players: MutableMap<Int, ExoPlayer>

    companion object {
        const val EXTRA_QUERY = "extra_query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_search)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.SearchFragment -> {
                    binding.tvSearch.isEnabled = true
                    binding.edtSearch.isEnabled = true
                    binding.ivMore.isEnabled = false
                    binding.ivMore.visibility = View.GONE
                    binding.tvSearch.visibility = View.VISIBLE

                    binding.edtSearch.post {
                        binding.edtSearch.requestFocus()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.edtSearch, InputMethodManager.SHOW_IMPLICIT)
                    }
                }

                R.id.SearchSuccessFragment -> {
                    binding.tvSearch.isEnabled = false
                    binding.edtSearch.isEnabled = false

                    binding.ivMore.isEnabled = true
                    binding.ivMore.visibility = View.VISIBLE
                    binding.tvSearch.visibility = View.GONE

                    binding.edtSearch.post {
                        binding.edtSearch.clearFocus()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
                    }

                }
            }
        }

        binding.tvSearch.setOnClickListener {
            val keyword = binding.edtSearch.text.toString().trim()
            val action =
                SearchFragmentDirections
                    .actionSearchFragmentToSearchSuccessFragment(keyword)
            navController.navigate(action)
        }

        val query = intent.getStringExtra(EXTRA_QUERY) ?: ""
        binding.ivBack.setOnClickListener {
            if (!navController.popBackStack() || query.isNotEmpty()) {
                finish()
                overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
            }
        }

        //Nút backPress
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!navController.popBackStack() || query.isNotEmpty()) {
                    finish()
                    overridePendingTransition(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
                }
            }

        })

        //Intent
        if (query.isNotEmpty()) {
            val action = SearchFragmentDirections
                .actionSearchFragmentToSearchSuccessFragment(query)

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.SearchFragment, inclusive = true) // false = giữ SearchFragment
                .build()

            navController.navigate(action.actionId, action.arguments, navOptions)

            binding.edtSearch.post {
                binding.edtSearch.setText(query)
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_search)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}