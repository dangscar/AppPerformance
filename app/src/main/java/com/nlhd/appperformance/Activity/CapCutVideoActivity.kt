package com.nlhd.appperformance.Activity

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.nlhd.appperformance.databinding.ActivityCapcutVideoBinding
import dagger.hilt.android.AndroidEntryPoint
import com.nlhd.appperformance.R

@AndroidEntryPoint
class CapCutVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCapcutVideoBinding
    private var player: ExoPlayer? = null

    companion object {
        const val EXTRA_VIDEO_URL = "extra_video_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCapcutVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL) ?: ""

        setupUI()
        if (videoUrl.isNotEmpty()) {
            initializePlayer(videoUrl)
        } else {
            // Có thể xử lý khi link trống ở đây
        }
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        binding.btnUseTemplate.setOnClickListener {
            // Logic to open CapCut or handle template usage
        }

        binding.videoCard.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    binding.ivPlay.visibility = View.VISIBLE
                } else {
                    it.play()
                    binding.ivPlay.visibility = View.GONE
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(videoUrl: String) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            
            val mediaItem = MediaItem.fromUri(videoUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            exoPlayer.playWhenReady = true
        }

        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                binding.ivPlay.visibility = if (isPlaying) View.GONE else View.VISIBLE
            }
        })
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
