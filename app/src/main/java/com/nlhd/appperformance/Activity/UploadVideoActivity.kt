package com.nlhd.appperformance.Activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.ViewModel.UploadVideoViewModel
import com.nlhd.appperformance.databinding.ActivityUploadVideoBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.URI

@AndroidEntryPoint
class UploadVideoActivity : AppCompatActivity() {
    private var _binding: ActivityUploadVideoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UploadVideoViewModel by viewModels()

    private var videoUri = Uri.EMPTY
    private val pickVideo =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Glide.with(this).load(it).into(binding.ivVideo)
                videoUri = it
                Log.d("AAA", it.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityUploadVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Nút backPress
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }
        })

        binding.flVideo.setOnClickListener {
            pickVideo.launch("video/*")
        }

        binding.btnPost.setOnClickListener {
            if (videoUri == Uri.EMPTY) return@setOnClickListener
            lifecycleScope.launch {
                viewModel.userState.collect { userPreference ->
                    if (!userPreference.isLoggedIn) {

                    } else {
                        if (userPreference.token.isNotEmpty()){
                            viewModel.uploadVideo(userPreference.token, videoUri, videoUri, binding.edtDescription.text.toString(), this@UploadVideoActivity)
                        }
                    }

                }
            }
        }
        viewModel.state.observe(this) {
            when (it) {
                is ResultUI.Error<*> -> {
                    binding.llLoading.visibility = android.view.View.GONE
                    Log.d("AAA", it.message.toString())
                }
                ResultUI.Idle -> {
                    binding.llLoading.visibility = android.view.View.GONE
                }
                ResultUI.Loading -> {
                    binding.llLoading.visibility = android.view.View.VISIBLE
                }
                is ResultUI.Success<*> -> {
                    binding.llLoading.visibility = android.view.View.GONE
                    Log.d("AAA", it.data.toString())
                    val message = it.data as MessageResponse
                    if (message.message == "Video created successfully") {
                        finish()
                        overridePendingTransition(
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                    }
                }
            }
        }

    }
}