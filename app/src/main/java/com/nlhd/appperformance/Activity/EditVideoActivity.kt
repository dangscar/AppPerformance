package com.nlhd.appperformance.Activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.effect.Brightness
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ClippingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.nlhd.appperformance.Adapter.ThumbnailAdapter
import com.nlhd.appperformance.databinding.ActivityEditVideoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.MDC.put
import java.io.File
import java.io.FileInputStream
import kotlin.apply
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.AudioEffects
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditVideoBinding
    private var player: ExoPlayer? = null
    private var audioUri: Uri? = null
    private var videoUri: Uri? = null

    @Inject
    lateinit var effectsMap: MutableMap<Int, AudioEffects>
    @Inject
    lateinit var players: MutableMap<Int, ExoPlayer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mock URI or get from intent
        val uriString = intent.getStringExtra("VIDEO_URI")
        if (uriString != null) {
            videoUri = Uri.parse(uriString)
        }
        releaseAllEffects()
        players.values.forEach { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        players.clear()

        binding.rvThumbnails.layoutManager = object : LinearLayoutManager(this, HORIZONTAL, false) {
                override fun canScrollHorizontally(): Boolean {
                    return false
                }
            }

        setupPlayer()
        setupUI()
        videoUri?.let { uri ->
            generateThumbnails(uri)
        }
        startProgress()
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI() {
        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener {
            Toast.makeText(this, "Đang lưu video...", Toast.LENGTH_SHORT).show()
            // Logic lưu video sau khi chỉnh sửa sẽ ở đây

            val videoDuration = player?.duration ?: 0L // milliseconds

            val timelineWidth =
                binding.trimmerContainer.width.toFloat()

// START
            val startPercent =
                binding.leftHandle.x / timelineWidth

            val startTime =
                (videoDuration * startPercent).toLong()

// END
            val endPercent =
                (binding.rightHandle.x + binding.rightHandle.width) / timelineWidth

            val endTime =
                (videoDuration * endPercent).toLong()

            Log.d("AAA", "startTime = $startTime")
            Log.d("AAA", "endTime = $endTime")
            /*trimVideo(
                videoUri!!,
                startTime,
                endTime
            )*/
            //updatePlayerTrim(startTime, endTime)
            //resetTrimUI()
        }

        binding.playerView.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    binding.ivPlayPause.visibility = View.VISIBLE
                    binding.btnPlaySmall.setImageResource(R.drawable.ic_play)
                } else {
                    it.play()
                    binding.ivPlayPause.visibility = View.GONE
                    binding.btnPlaySmall.setImageResource(R.drawable.ic_pause)
                }
            }
        }

        binding.leftHandle.setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_MOVE -> {

                    val location = IntArray(2)
                    binding.trimmerContainer.getLocationOnScreen(location)

                    var newX = event.rawX - location[0]

                    // MIN
                    if (newX < 0f) {
                        newX = 0f
                    }

                    // MAX
                    val maxX =
                        binding.trimmerContainer.width -
                                binding.leftHandle.width

                    if (newX > maxX) {
                        newX = maxX.toFloat()
                    }

                    // KHÔNG vượt rightHandle
                    val maxLeftX =
                        binding.rightHandle.x -
                                binding.leftHandle.width

                    if (newX > maxLeftX) {
                        newX = maxLeftX
                    }

                    /*
                     * UPDATE HANDLE
                     */
                    binding.leftHandle.x = newX

                    binding.overlayLeft.layoutParams.width =
                        binding.leftHandle.x.toInt()

                    binding.trimFrame.requestLayout()

                    /*
                     * SEEK PLAYER
                     */
                    val duration = player?.duration ?: 0L

                    val percent =
                        binding.leftHandle.x /
                                binding.trimmerContainer.width.toFloat()

                    val seekTo =
                        (duration * percent).toLong()

                    player?.seekTo(seekTo)

                    /*
                     * MOVE PLAYHEAD
                     */
                    binding.playHead.x =
                        binding.leftHandle.x +
                                binding.leftHandle.width

                    /*
                     * UPDATE UI TIME
                     */
                    updateDurationUI(seekTo)
                }
            }

            true
        }

        binding.rightHandle.setOnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_MOVE -> {

                    val location = IntArray(2)
                    binding.trimmerContainer.getLocationOnScreen(location)

                    var newX = event.rawX - location[0]

                    // giới hạn min
                    if (newX < 0f) {
                        newX = 0f
                    }

                    // giới hạn max
                    val maxX =
                        binding.trimmerContainer.width -
                                binding.rightHandle.width

                    if (newX > maxX) {
                        newX = maxX.toFloat()
                    }

                    // không vượt leftHandle
                    val minRightX = binding.leftHandle.x + binding.leftHandle.width

                    if (newX < minRightX) {
                        newX = minRightX
                    }

                    binding.rightHandle.x = newX

                    val rightStart = binding.rightHandle.x + binding.rightHandle.width
                    binding.overlayRight.layoutParams.width = (binding.trimmerContainer.width - rightStart).toInt()
                    binding.trimFrame.requestLayout()
                }
            }

            true
        }

        binding.playHead.setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE -> {

                    val location = IntArray(2)
                    binding.trimmerContainer.getLocationOnScreen(location)

                    var newX = event.rawX - location[0]

                    // Giới hạn playhead trong vùng trim
                    val minX = binding.leftHandle.x + binding.leftHandle.width

                    val maxX = binding.rightHandle.x

                    if (newX < minX) {
                        newX = minX
                    }

                    if (newX > maxX) {
                        newX = maxX
                    }

                    binding.playHead.x = newX

                    /*
                     * SEEK PLAYER
                     */

                    val duration = player?.duration ?: 0L

                    // % trong vùng trim
                    val percent = (newX - minX) / (maxX - minX)

                    // thời gian start/end thực tế
                    val startPercent = binding.leftHandle.x / binding.trimmerContainer.width.toFloat()

                    val endPercent = (binding.rightHandle.x + binding.rightHandle.width) / binding.trimmerContainer.width.toFloat()

                    val startTime = duration * startPercent

                    val endTime = duration * endPercent

                    // map playhead -> time
                    val seekTo = startTime + ((endTime - startTime) * percent)

                    player?.seekTo(seekTo.toLong())
                    updateDurationUI(seekTo.toLong())
                }
            }

            true
        }

        binding.trimmerContainer.setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE -> {

                    val touchX = event.x

                    // vùng trim
                    val minX =
                        binding.leftHandle.x +
                                binding.leftHandle.width

                    val maxX =
                        binding.rightHandle.x

                    // giới hạn touch trong vùng trim
                    val constrainedX =
                        touchX.coerceIn(minX, maxX)

                    // move playhead
                    binding.playHead.x = constrainedX

                    /*
                     * SEEK PLAYER
                     */

                    val duration = player?.duration ?: 0L

                    if (duration > 0) {

                        // thời gian trim
                        val startPercent =
                            binding.leftHandle.x /
                                    binding.trimmerContainer.width.toFloat()

                        val endPercent =
                            (binding.rightHandle.x + binding.rightHandle.width) /
                                    binding.trimmerContainer.width.toFloat()

                        val startTime =
                            duration * startPercent

                        val endTime =
                            duration * endPercent

                        // % trong vùng trim
                        val percent =
                            (constrainedX - minX) / (maxX - minX)

                        // map x -> time
                        val seekTo =
                            startTime +
                                    ((endTime - startTime) * percent)

                        player?.seekTo(seekTo.toLong())
                        updateDurationUI(seekTo.toLong())
                    }
                }
            }

            true
        }

        binding.rvThumbnails.setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE -> {

                    val touchX = event.x

                    // vùng trim
                    val minX =
                        binding.leftHandle.x +
                                binding.leftHandle.width

                    val maxX =
                        binding.rightHandle.x

                    // giới hạn touch trong vùng trim
                    val constrainedX =
                        touchX.coerceIn(minX, maxX)

                    // move playhead
                    binding.playHead.x = constrainedX

                    /*
                     * SEEK PLAYER
                     */

                    val duration = player?.duration ?: 0L

                    if (duration > 0) {

                        // thời gian trim
                        val startPercent =
                            binding.leftHandle.x /
                                    binding.trimmerContainer.width.toFloat()

                        val endPercent =
                            (binding.rightHandle.x + binding.rightHandle.width) /
                                    binding.trimmerContainer.width.toFloat()

                        val startTime =
                            duration * startPercent

                        val endTime =
                            duration * endPercent

                        // % trong vùng trim
                        val percent =
                            (constrainedX - minX) / (maxX - minX)

                        // map x -> time
                        val seekTo =
                            startTime +
                                    ((endTime - startTime) * percent)

                        player?.seekTo(seekTo.toLong())
                        updateDurationUI(seekTo.toLong())
                    }
                }
            }

            true
        }

        binding.btnAudioTool.setOnClickListener {

            pickAudioLauncher.launch("audio/*")
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer() {

        player = ExoPlayer.Builder(this).build()

        binding.playerView.player = player

        player?.repeatMode = Player.REPEAT_MODE_ALL

        /*
         * AUDIO EFFECT
         */
        player?.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady) {
                    binding.btnPlaySmall.setImageResource(R.drawable.ic_pause)
                } else {
                    binding.btnPlaySmall.setImageResource(R.drawable.ic_play)
                }
            }
            override fun onAudioSessionIdChanged(
                audioSessionId: Int
            ) {

                if (audioSessionId == AudioManager.ERROR) {
                    return
                }

                try {


                    /*
                     * EQUALIZER
                     */
                    equalizer =
                        Equalizer(0, audioSessionId).apply {

                            enabled = true

                            for (i in 0 until numberOfBands) {

                                val freq =
                                    getCenterFreq(i.toShort()) / 1000

                                when {

                                    freq < 200 -> {

                                        // bass
                                        setBandLevel(
                                            i.toShort(),
                                            1900.toShort()
                                        )
                                        Log.d("AAA", "<200")
                                    }

                                    freq in 200..1200 -> {

                                        // vocal
                                        setBandLevel(
                                            i.toShort(),
                                            800.toShort()
                                        )
                                        Log.d("AAA", "200..2000")
                                    }

                                    else -> {

                                        // treble
                                        setBandLevel(
                                            i.toShort(),
                                            0.toShort()
                                        )
                                        Log.d("AAA", "else")
                                    }
                                }
                            }
                        }

                    /*
                     * BASS BOOST
                     */
                    bassBoost =
                        BassBoost(0, audioSessionId).apply {

                            setStrength(1000.toShort())

                            enabled = true
                        }

                    /*
                     * LOUDNESS
                     */
                    loudnessEnhancer =
                        LoudnessEnhancer(audioSessionId).apply {

                            setTargetGain(1800)

                            enabled = true
                        }
                    Log.d("AAA", "Yes")

                } catch (e: Exception) {
                    Log.d("AAA", e.message.toString())
                    e.printStackTrace()
                }
            }
        })

        /*
         * VIDEO EFFECT
         */
        val brightnessEffect = Brightness(0.1f)

        player?.setVideoEffects(
            listOf(brightnessEffect)
        )

        /*
         * MEDIA
         */
        videoUri?.let {

            val mediaItem = MediaItem.fromUri(it)

            player?.setMediaItem(mediaItem)

            player?.prepare()

            player?.play()
        }
    }

    private fun updateDurationUI(currentTime: Long) {

        val duration = player?.duration ?: return

        val containerWidth = binding.trimmerContainer.width.toFloat()

        val startPercent =
            binding.leftHandle.x / containerWidth

        val endPercent =
            (binding.rightHandle.x + binding.rightHandle.width) / containerWidth

        val startTime = (duration * startPercent).toLong()
        val endTime = (duration * endPercent).toLong()

        val trimDuration = endTime - startTime

        val playHeadTime = currentTime - startTime

        binding.tvCurrentTime.text = formatTime(playHeadTime)
        binding.tvTotalTrimTime.text = formatTime(trimDuration)
    }
    private fun updatePlayerTrim(
        startMs: Long,
        endMs: Long,
    ) {

        val uri = videoUri ?: return

        val mediaItem =
            MediaItem.Builder()
                .setUri(uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(startMs)
                        .setEndPositionMs(endMs)
                        .build()
                )
                .build()

        player?.apply {

            setMediaItem(mediaItem)

            prepare()

            play()
        }
    }

    private fun resetTrimUI() {

        binding.trimmerContainer.post {

            /*
             * LEFT HANDLE
             */
            binding.leftHandle.x = 0f

            /*
             * RIGHT HANDLE
             */
            binding.rightHandle.x =
                binding.trimmerContainer.width -
                        binding.rightHandle.width.toFloat()

            /*
             * PLAYHEAD
             */
            binding.playHead.x =
                binding.leftHandle.width.toFloat()

            /*
             * OVERLAY
             */
            binding.overlayLeft.layoutParams.width = 0

            binding.overlayRight.layoutParams.width = 0

            binding.overlayLeft.requestLayout()
            binding.overlayRight.requestLayout()

            /*
             * DURATION UI
             */
            updateDurationUI(0L)
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }

    private fun generateThumbnails(videoUri: Uri) {
        binding.trimmerContainer.post {

            lifecycleScope.launch(Dispatchers.IO) {

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this@EditVideoActivity, videoUri)

                val duration =
                    retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_DURATION
                    )!!.toLong()

                val list = mutableListOf<Bitmap>()

                // width thực tế timeline
                val containerWidth =
                    binding.trimmerContainer.width

                // mỗi thumbnail rộng bao nhiêu dp
                val thumbnailWidth =
                    (40 * resources.displayMetrics.density).toInt()

                // số lượng thumbnail cần
                val frameCount = (containerWidth / thumbnailWidth) + 1

                val interval =
                    duration / frameCount

                for (i in 0 until frameCount) {

                    val bitmap = retriever.getFrameAtTime(
                        i * interval * 1000,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )

                    bitmap?.let {
                        list.add(it)
                    }
                }

                retriever.release()

                withContext(Dispatchers.Main) {

                    binding.rvThumbnails.adapter =
                        ThumbnailAdapter(list)
                }
            }
        }
    }

    private fun startProgress() {

        val handler = Handler(Looper.getMainLooper())

        handler.post(object : Runnable {

            override fun run() {

                val duration = player?.duration ?: 0L
                val current = player?.currentPosition ?: 0L

                if (duration > 0) {

                    val startX = binding.leftHandle.x + binding.leftHandle.width

                    val endX = binding.rightHandle.x

                    val startPercent = binding.leftHandle.x / binding.trimmerContainer.width.toFloat()

                    val endPercent = (binding.rightHandle.x + binding.rightHandle.width) / binding.trimmerContainer.width.toFloat()

                    val startTime = duration * startPercent

                    val endTime = duration * endPercent

                    /*
                     * LOOP TRIM
                     */
                    if (current >= endTime.toLong()) {
                        player?.seekTo(startTime.toLong())
                    }

                    val trimDuration = endTime - startTime

                    if (trimDuration > 0) {

                        val percent = (current - startTime) / trimDuration

                        val playHeadX = startX + ((endX - startX) * percent)

                        binding.playHead.x = playHeadX.coerceIn(startX, endX)
                    }
                    updateDurationUI(current)
                }

                handler.postDelayed(this, 16)
            }
        })
    }
    private var equalizer: Equalizer? = null

    private var bassBoost: BassBoost? = null

    private var loudnessEnhancer: LoudnessEnhancer? = null

    @OptIn(UnstableApi::class)
    private fun trimVideo(
        inputUri: Uri,
        startMs: Long,
        endMs: Long
    ) {

        val mediaItem = MediaItem.Builder()
            .setUri(inputUri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startMs)
                    .setEndPositionMs(endMs)
                    .build()
            )
            .build()

        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .build()

        val outputFile = File(
            cacheDir,
            "trimmed_${System.currentTimeMillis()}.mp4"
        )


        val transformer = Transformer.Builder(this)
            .addListener(object : Transformer.Listener {

                override fun onCompleted(
                    composition: Composition,
                    exportResult: ExportResult
                ) {
                    saveVideoToDownloads(outputFile)
                    Log.d("AAA", outputFile.absolutePath)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {

                    exportException.printStackTrace()
                }
            })
            .build()

        transformer.start(
            editedMediaItem,
            outputFile.absolutePath
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveVideoToDownloads(file: File) {

        val values = ContentValues().apply {
            put(
                MediaStore.Downloads.DISPLAY_NAME,
                "trimmed_${System.currentTimeMillis()}.mp4"
            )

            put(
                MediaStore.Downloads.MIME_TYPE,
                "video/mp4"
            )

            put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )

            put(
                MediaStore.Downloads.IS_PENDING,
                1
            )
        }

        val uri = contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        ) ?: return

        contentResolver.openOutputStream(uri)?.use { output ->
            FileInputStream(file).use { input ->
                input.copyTo(output)
            }
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)

        contentResolver.update(uri, values, null, null)

        Log.d("AAA", "Saved to Downloads")
    }

    private val pickAudioLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {
                addAudioToPlayer(uri)
            }
        }

    @OptIn(UnstableApi::class)
    private fun addAudioToPlayer(uri: Uri) {

        audioUri = uri

        val video = videoUri ?: return

        val currentPosition = player?.currentPosition ?: 0L
        val isPlaying = player?.isPlaying ?: false

        val dataSourceFactory = DefaultDataSource.Factory(this)

        /*
         * VIDEO SOURCE — strip audio bằng DefaultTrackSelector
         */
        val videoSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(video))

        /*
         * AUDIO SOURCE
         */
        val audioItem = MediaItem.Builder()
            .setUri(uri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(0L)
                    .setEndPositionMs(5000L)
                    .build()
            )
            .build()


        val audioSource =
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(audioItem)

        /*
         * MERGE
         */
        val mergedSource = MergingMediaSource(videoSource, audioSource)

        player?.apply {
            stop()
            clearMediaItems()
            setMediaSource(mergedSource)
            prepare()
            seekTo(currentPosition)
            if (isPlaying) play()
        }

        /*
         * FORCE chọn audio từ audioSource (group index 1)
         * và disable audio track trong videoSource (group index 0)
         */
        forceSelectAudioTrack()
    }

    @OptIn(UnstableApi::class)
    private fun forceSelectAudioTrack() {
        player?.addListener(object : Player.Listener {

            override fun onTracksChanged(tracks: Tracks) {

                val audioGroups = tracks.groups.filter {
                    it.type == C.TRACK_TYPE_AUDIO
                }

                Log.d("TRACKS", "Audio groups found: ${audioGroups.size}")

                if (audioGroups.size < 2) return  // Chưa load đủ track, chờ

                // audioGroups[0] = audio từ videoSource (nếu có)
                // audioGroups[1] = audio từ audioSource (file nhạc)

                val overrides = mutableMapOf<TrackGroup, TrackSelectionOverride>()

                // Disable audio group đầu tiên (từ video)
                overrides[audioGroups[0].mediaTrackGroup] =
                    TrackSelectionOverride(
                        audioGroups[0].mediaTrackGroup,
                        emptyList()  // emptyList = disabled
                    )

                // Force select audio group thứ hai (file nhạc)
                overrides[audioGroups[1].mediaTrackGroup] =
                    TrackSelectionOverride(
                        audioGroups[1].mediaTrackGroup,
                        listOf(0)   // chọn track index 0
                    )

                player?.trackSelectionParameters =
                    player!!.trackSelectionParameters
                        .buildUpon()
                        .setOverrideConditionsForTrackGroup(overrides)
                        .build()

                // Remove listener sau khi đã set xong
                player?.removeListener(this)
            }
        })
    }

    // Extension function tiện dụng
    @OptIn(UnstableApi::class)
    private fun TrackSelectionParameters.Builder.setOverrideConditionsForTrackGroup(
        overrides: Map<TrackGroup, TrackSelectionOverride>
    ): TrackSelectionParameters.Builder {
        overrides.forEach { (_, override) ->
            addOverride(override)
        }
        return this
    }

    /*private fun showAudioTimingDialog(uri: Uri) {

        var startMs = 0L
        var endMs = C.TIME_UNSET

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }

        val startInput = EditText(this).apply {
            hint = "Start (ms), mặc định = 0"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val endInput = EditText(this).apply {
            hint = "End (ms), để trống = phát hết"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(TextView(this).apply { text = "⏱ Thời gian bắt đầu" })
        layout.addView(startInput)
        layout.addView(TextView(this).apply {
            text = "⏹ Thời gian kết thúc"
            setPadding(0, 16, 0, 0)
        })
        layout.addView(endInput)

        AlertDialog.Builder(this)
            .setTitle("Cắt audio")
            .setView(layout)
            .setPositiveButton("Xác nhận") { _, _ ->

                startMs = startInput.text.toString()
                    .toLongOrNull() ?: 0L

                endMs = endInput.text.toString()
                    .toLongOrNull() ?: C.TIME_UNSET

                // Validate
                if (endMs != C.TIME_UNSET && endMs <= startMs) {
                    Toast.makeText(
                        this,
                        "End phải lớn hơn Start",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                addAudioToPlayer(uri, startMs, endMs)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }*/


    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        equalizer?.release()
        equalizer = null
        bassBoost?.release()
        bassBoost = null
        loudnessEnhancer?.release()
        loudnessEnhancer = null
    }

    private fun releaseAllEffects() {

        effectsMap.values.forEach { effects ->

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

        effectsMap.clear()
    }
}
