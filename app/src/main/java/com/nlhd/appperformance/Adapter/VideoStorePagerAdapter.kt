package com.nlhd.appperformance.Adapter

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.TimeBar
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nlhd.appperformance.Activity.CapCutVideoActivity
import com.nlhd.appperformance.Domain.Entity.VideoStore
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.AudioEffects
import com.nlhd.appperformance.databinding.ItemVideoBinding
import kotlin.math.abs

@UnstableApi
class VideoStorePagerAdapter(
    private val context: Context,
    private val defaultMediaSourceFactory: DefaultMediaSourceFactory,
    private val players : MutableMap<Int, ExoPlayer>,
    private val effectsMap: MutableMap<Int, AudioEffects>,
    private val onClickComment: (Int) -> Unit
) : PagingDataAdapter<VideoStore, VideoStorePagerAdapter.VideoViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<VideoStore>() {
            override fun areItemsTheSame(oldItem: VideoStore, newItem: VideoStore) = oldItem.uri == newItem.uri
            override fun areContentsTheSame(oldItem: VideoStore, newItem: VideoStore) = oldItem == newItem
        }
    }

    inner class VideoViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.root.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return VideoViewHolder(binding)
    }

    private fun listener(onPlay: () -> Unit, onPause: () -> Unit) = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_READY) {
                onPlay()
            } else {
                onPause()
            }
        }
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            super.onAudioSessionIdChanged(audioSessionId)

            if (audioSessionId == AudioManager.ERROR) return

            if (effectsMap.containsKey(audioSessionId)) {
                return
            }

            try {

                val equalizer =
                    Equalizer(0, audioSessionId).apply {

                        enabled = true

                        for (i in 0 until numberOfBands) {

                            val freq = getCenterFreq(i.toShort()) / 1000

                            when {
                                freq < 200 -> {
                                    setBandLevel(i.toShort(), 1900)
                                }

                                freq in 200..1200 -> {
                                    setBandLevel(i.toShort(), 800)
                                }

                                else -> {
                                    setBandLevel(i.toShort(), 0)
                                }
                            }
                        }
                    }

                val bassBoost =
                    BassBoost(0, audioSessionId).apply {

                        setStrength(1000.toShort())
                        enabled = true
                    }

                val loudness =
                    LoudnessEnhancer(audioSessionId).apply {

                        setTargetGain(1800)
                        enabled = true
                    }

                effectsMap[audioSessionId] =
                    AudioEffects(
                        equalizer,
                        bassBoost,
                        loudness
                    )
                Log.d("AAA", effectsMap.toString())

            } catch (e: Exception) {
                Log.e("AAA", "Create effect error", e)
            }
        }
    }

    /*fun playStateExoPlayer(exoPlayer: ExoPlayer, onPlay: ()-> Unit, onPause: ()-> Unit) {
        exoPlayer.addListener(listener(
            onPlay = onPlay,
            onPause = onPause
        ))
    }*/

    var currentPosition = RecyclerView.NO_POSITION

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = getItem(position) ?: return
        createPlayer(position)
        holder.binding.playerView.player = players[position]

        Glide.with(holder.binding.ivAvatar).load("").error(R.drawable.asus).into(holder.binding.ivAvatar)

        holder.binding.txtLike.text = "14,5 N"
        holder.binding.txtComment.text = "50"
        holder.binding.txtFavorite.text = "1.599"
        holder.binding.txtShare.text = "450"

        holder.binding.apply {
            txtUsername.text = video.name
            txtDescription.text = "Not description"
        }
        holder.binding.llComment.setOnClickListener {
            onClickComment(1)
        }
        
        // Capcut intent
        holder.binding.llCapcut.setOnClickListener {
            val intent = Intent(context, CapCutVideoActivity::class.java).apply {
                putExtra(CapCutVideoActivity.EXTRA_VIDEO_URL, video.uri.toString())
            }
            context.startActivity(intent)
        }

        holder.binding.playSpeedRight.setOnTouchListener { v, event ->
            val player = players[position] ?: return@setOnTouchListener false

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    player.playbackParameters = PlaybackParameters(2f)
                    holder.binding.apply {
                        actionColumn.fadeTo(0f)
                        bottomInfo.fadeTo(0f)
                        seekBar.fadeTo(0f)
                    }
                    true
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    player.playbackParameters = PlaybackParameters(1f)
                    holder.binding.apply {
                        actionColumn.fadeTo(1f)
                        bottomInfo.fadeTo(1f)
                        seekBar.fadeTo(1f)
                    }
                    true
                }
                else -> false
            }
        }
        holder.binding.aspectLayout.setOnClickListener {
            val player = players[position] ?: return@setOnClickListener
            player.playWhenReady = !player.playWhenReady
        }

        setupTimeBar(holder, players[position]!!)
    }

    fun getViewPage(position: Int): String {
        val item = getItem(position) ?: return "Loading"
        return item.name
    }

    fun View.fadeTo(
        alpha: Float,
        duration: Long = 280L
    ) {
        animate()
            .alpha(alpha)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }


    fun createPlayer(current: Int) {
        if (players.size >= 3 && currentPosition != current) {
            releaseFarthestPlayer()
        }

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1500,  // minBufferMs (mặc định ~50_000)
                3000,  // maxBufferMs (mặc định ~50_000)
                500,   // bufferForPlaybackMs
                1000   // bufferForPlaybackAfterRebufferMs
            )
            .build()
        val video = getItem(current)!!
        players.getOrPut(current) {
            ExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .build().apply {
                    addListener(listener(
                        onPlay = {},
                        onPause = {}
                    ))
                    repeatMode = ExoPlayer.REPEAT_MODE_ONE
                    setMediaItem(MediaItem.fromUri(video.uri))
                    prepare()
                    playWhenReady = false
                }
        }
    }

    fun play(position: Int) {
        if (players.contains(position)) {
            players[position]?.playWhenReady = true
        }
    }

    fun pause(position: Int) {
        if (players.contains(position)) {
            players[position]?.playWhenReady = false
        }
    }

    fun seekToStart(position: Int) {
        if (players.contains(position)) {
            players[position]!!.seekTo(0)
        }
    }

    fun resetAllPlayerExceptPos(position: Int) {
        players.forEach { (pos, player) ->
            if (pos != position) {
                val currentItem = player.currentMediaItem ?: return@forEach
                player.setMediaItem(currentItem)
                player.prepare()
            }
        }
    }

    fun releaseAllPlayers() {
        players.values.forEach { player ->
            releaseEffects(player)
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        players.clear()
        effectsMap.clear()
    }

    fun handlePlayerState(current: Int) {
        if (players.contains(current)) {
            currentPosition = current
            val keepRange = (current - 1)..(current + 1)

            players.forEach { (pos, player) ->
                when {
                    pos == current -> {
                        player.playWhenReady = true
                    }
                    pos in keepRange -> {
                        player.seekTo(0)
                        player.playWhenReady = false
                    }
                    else -> {
                        player.seekTo(0)
                        player.playWhenReady = false
                    }
                }
            }
        }
    }

    fun handlePlayerPlay(position: Int) {
        val player = players[position] ?: return
        if (player.isPlaying) {
            player.playWhenReady = false
        } else {
            player.playWhenReady = true
        }
    }

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (currentPosition == holder.bindingAdapterPosition) return
        val player = players[holder.bindingAdapterPosition] ?: return
        resetPlayer(player, holder.bindingAdapterPosition, holder)
    }

    fun resetPlayer(player: ExoPlayer, position: Int, holder: VideoStorePagerAdapter.VideoViewHolder) {
        val video = getItem(position)
        player.setMediaItem(MediaItem.fromUri(video!!.uri))
        player.prepare()
        holder.binding.playerView.player = player
    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (currentPosition == holder.bindingAdapterPosition) return
        var player = players[holder.bindingAdapterPosition]
        val video = getItem(holder.bindingAdapterPosition)
        player?.setMediaItem(MediaItem.fromUri(video!!.uri))
        player?.prepare()
        holder.binding.playerView.player = player
    }

    fun releaseFarthestPlayer() {
        if (players.size < 3) return
        val farthestPosition = players.keys
            .maxByOrNull { abs(it - currentPosition) }

        farthestPosition?.let { position ->
            players[position]?.let { player ->
                releaseEffects(player)
            }
            players[position]?.release()
            players.remove(position)
        }
    }

    fun setupTimeBar(holder: VideoViewHolder, exoPlayer: ExoPlayer) {
        val seekBar = holder.binding.seekBar
        seekBar.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {}
            override fun onScrubMove(timeBar: TimeBar, position: Long) {}
            override fun onScrubStop(
                timeBar: TimeBar,
                position: Long,
                canceled: Boolean
            ) {
                if (!canceled) {
                    exoPlayer.seekTo(position)
                }
                exoPlayer.playWhenReady = true
            }
        })
        startProgressUpdate(holder, exoPlayer)
    }

    fun startProgressUpdate(holder: VideoViewHolder, exoPlayer: ExoPlayer) {
        val seekBar = holder.binding.seekBar
        val runnable = object : Runnable {
            override fun run() {
                if (!exoPlayer.isReleased) {
                    seekBar.setDuration(exoPlayer.duration.coerceAtLeast(0))
                    seekBar.setPosition(exoPlayer.currentPosition)
                    seekBar.postDelayed(this, 500)
                }
            }
        }
        seekBar.tag = runnable
        seekBar.post(runnable)
    }

    fun updateCurrentPosition(position: Int) {
        currentPosition = position
        Log.d("AAA", position.toString())
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.bindingAdapterPosition
        if (position == currentPosition) return
        if (position != RecyclerView.NO_POSITION) {
            players[position]?.let { player ->
                releaseEffects(player)
            }
            players[position]?.release()
            players.remove(position)
            holder.binding.seekBar.tag?.let {
                holder.binding.seekBar.removeCallbacks(it as Runnable)
            }
        }
    }

    private fun releaseEffects(player: ExoPlayer) {

        val sessionId = player.audioSessionId

        effectsMap.remove(sessionId)?.let { effects ->

            try {
                effects.equalizer.release()
            } catch (_: Exception) {
            }

            try {
                effects.bassBoost.release()
            } catch (_: Exception) {
            }

            try {
                effects.loudnessEnhancer.release()
            } catch (_: Exception) {
            }
        }
    }
}
