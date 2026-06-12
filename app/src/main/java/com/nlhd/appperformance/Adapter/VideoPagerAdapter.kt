package com.nlhd.appperformance.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
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
import com.nlhd.appperformance.Activity.SearchActivity
import com.nlhd.appperformance.Domain.Entity.Video.Video
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.AudioEffects
import com.nlhd.appperformance.databinding.ItemVideoBinding

@UnstableApi
class VideoPagerAdapter(
    private val context: Context,
    private val defaultMediaSourceFactory: DefaultMediaSourceFactory,
    private val players : MutableMap<Int, ExoPlayer>,
    private val effectsMap: MutableMap<Int, AudioEffects>,
    private val onClickComment: (Int) -> Unit,
    private val onClickLike: (Int, Int) -> Unit,
    private val onClickShare: () -> Unit,
    private val onClickProfile: (Int) -> Unit,
    private val onClickFollow: (Int) -> Unit
) : PagingDataAdapter<Video,VideoPagerAdapter.VideoViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Video, newItem: Video) = oldItem == newItem
        }
        const val PAYLOAD_FOLLOW = "payload_follow"
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

    var currentPosition = RecyclerView.NO_POSITION

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

    private fun bindFollow(
        holder: VideoViewHolder,
        video: Video
    ) {
        when (video.canFollow) {
            "1" -> {

                holder.binding.flFollowing.visibility =
                    if (video.isFollowing == "0") {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
            }
            else -> {
                holder.binding.flFollowing.visibility = View.INVISIBLE
            }
        }
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = getItem(position) ?: return

        //Set up player
        createPlayer(position)
        val player = players[position] ?: return
        holder.binding.playerView.player = player

        /*val textureView = holder.binding.playerView.videoSurfaceView as? TextureView

        textureView?.let {
            val paint = Paint()

            val colorMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, 25f,
                    0f, 1f, 0f, 0f, 25f,
                    0f, 0f, 1f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

            it.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        }*/

        //Avatar
        holder.binding.ivAvatar.setOnClickListener {
            onClickProfile(video.user.id)
        }
        Glide.with(holder.binding.ivAvatar).load(video.user.avatarUrl).error(R.drawable.asus).into(holder.binding.ivAvatar)
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                1.1f, 0f, 0f, 0f, 20f,  // Red
                0f, 1.1f, 0f, 0f, 20f,  // Green
                0f, 0f, 1.1f, 0f, 20f,  // Blue
                0f, 0f, 0f, 1.1f, 0f      // Alpha
            )
        )

        holder.binding.ivAvatar.colorFilter = ColorMatrixColorFilter(colorMatrix)

        //Follow
        bindFollow(holder, video)
        holder.binding.flFollowing.setOnClickListener {
            onClickFollow(video.user.id)
        }


        //Like
        updateLike(holder, video)
        holder.binding.llLike.setOnClickListener {
            onClickLike(video.id, position)
        }

        holder.binding.txtLike.text = video.likesCount
        holder.binding.txtComment.text = video.commentsCount
        holder.binding.txtFavorite.text = video.favoritesCount
        holder.binding.txtShare.text = if (video.shares == "0") "Shares" else video.shares

        holder.binding.llShare.setOnClickListener {
            onClickShare()
        }

        //Capcut
        holder.binding.llCapcut.setOnClickListener {
            val intent = Intent(context, CapCutVideoActivity::class.java).apply {
                putExtra(CapCutVideoActivity.EXTRA_VIDEO_URL, video.videoUrl)
            }
            context.startActivity(intent)
        }

        val username = video.user.name
        val time = " • 2 giờ trước"

        val fullText = username + time

        val spannable = SpannableStringBuilder(fullText)

        // Làm nhỏ phần time (ví dụ còn 80%)
        spannable.setSpan(
            RelativeSizeSpan(0.8f),
            username.length,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

// Đổi màu phần time
        spannable.setSpan(
            ForegroundColorSpan(0x83FAFAFA.toInt()),
            username.length,
            fullText.length,
            Spanned.SPAN_PRIORITY
        )

        holder.binding.txtUsername.text = username
        if (video.caption.isNotEmpty()) {
            formatDescription(holder.binding.txtDescription, video.caption)
        } else {
            holder.binding.txtDescription.visibility = View.GONE
        }
        holder.binding.llComment.setOnClickListener {
            onClickComment(video.id)
        }
        holder.binding.aspectLayout.setOnClickListener {
            val player = players[position] ?: return@setOnClickListener
            player.playWhenReady = !player.playWhenReady
            holder.binding.ivPlayPause.visibility = if (player.isPlaying) View.INVISIBLE else View.VISIBLE
        }

        setupTimeBar(holder, players[position]!!)

        holder.binding.txtContent.text = "Tìm kiếm videoId: "+video.id
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, payloads: List<Any?>) {
        if (payloads.contains(PAYLOAD_FOLLOW)) {

            val video = getItem(position) ?: return

            bindFollow(holder, video)

            return
        }

        super.onBindViewHolder(holder, position, payloads)

    }
    fun videoByPosition(position: Int): Video? {
        val video = getItem(position) ?: return null
        return video
    }

    fun updateFollowByUserId(
        userId: Int,
        isFollowing: String
    ) {

        snapshot().items.forEachIndexed { index, video ->
            if (video.user.id == userId) {
                video.isFollowing = isFollowing
                notifyItemChanged(index, PAYLOAD_FOLLOW)
            }
        }
    }

    fun updateLike(holder: VideoViewHolder, video: Video) {
        holder.binding.btnLike.setImageResource(if (video.isLiked == "1") R.drawable.ic_hearted else R.drawable.ic_heart)
        holder.binding.txtLike.text = video.likesCount
    }

    fun setActionPlayPause(holder: VideoViewHolder, isPlaying: Boolean) {
        holder.binding.ivPlayPause.visibility = if (isPlaying) View.INVISIBLE else View.VISIBLE
    }

    fun getViewPage(position: Int): String {
        val item = getItem(position) ?: return "Loading"
        return item.caption
    }

    fun createPlayer(current: Int) {
        // ✅ Guard: item chưa load xong thì bỏ qua
        val video = getItem(current) ?: return

        // ✅ Player đã tồn tại thì không tạo lại
        if (players.containsKey(current)) return

        // ✅ Giới hạn số player, nhưng không release player đang active
        if (players.size >= 3) {
            releaseFarthestPlayer()
        }

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(1500, 3000, 500, 1000)
            .build()

        val player = ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(defaultMediaSourceFactory)
            .build().apply {
                addListener(listener(
                    onPlay = {},
                    onPause = {}
                ))
                repeatMode = ExoPlayer.REPEAT_MODE_ONE
                setMediaItem(MediaItem.fromUri(video.videoUrl))
                prepare()
                playWhenReady = false
            }

        players[current] = player
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
                    pos == current ->{
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
        if (!players.contains(position)) return
        if (players[position]!!.isPlaying) {
            players[position]!!.pause()
        } else {
            players[position]!!.play()
        }
    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (currentPosition == holder.bindingAdapterPosition) return
        val player = players[holder.bindingAdapterPosition] ?: return
        resetPlayer(player, holder.bindingAdapterPosition, holder)
    }

    fun resetPlayer(player: ExoPlayer, position: Int, holder: VideoViewHolder) {
        val video = getItem(position)
        player.setMediaItem(MediaItem.fromUri(video!!.videoUrl))
        player.prepare()
        holder.binding.playerView.player = player
    }

    fun releaseFarthestPlayer() {
        if (players.size < 3) return

        val farthestPosition = players.keys
            .maxByOrNull { kotlin.math.abs(it - currentPosition) }

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
            override fun onScrubStart(timeBar: TimeBar, position: Long) {

            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {

            }

            override fun onScrubStop(
                timeBar: TimeBar,
                position: Long,
                canceled: Boolean
            ) {
                if (!canceled) {
                    exoPlayer.seekTo(position)
                }
                exoPlayer.play()
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

    fun formatDescription(textView: TextView, text: String) {
        val spannable = SpannableString(text)
        val regex = Regex("([@#][A-Za-z0-9_]+)")

        regex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            val tag = match.value

            // Click
            val clickSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(context, SearchActivity::class.java).apply {
                        putExtra(SearchActivity.EXTRA_QUERY, tag) // truyền "#xuhuong" hoặc "@username"
                    }
                    context.startActivity(intent)
                }
                // Giữ màu chữ tự set, không dùng màu mặc định của ClickableSpan
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = Color.parseColor("#B3FAFAFA")
                    ds.isUnderlineText = false
                }
            }

            spannable.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        // Xóa highlight màu khi click
        textView.highlightColor = Color.TRANSPARENT
    }

    fun updateCurrentPosition(position: Int) {
        currentPosition = position
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

