package com.nlhd.appperformance.Activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.nlhd.appperformance.Adapter.ThumbnailAdapter
import com.nlhd.appperformance.databinding.ActivityEditVideoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.MDC.put
import java.io.File
import java.io.FileInputStream
import kotlin.apply

class EditVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditVideoBinding
    private var player: ExoPlayer? = null
    private var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mock URI or get from intent
        val uriString = intent.getStringExtra("VIDEO_URI")
        if (uriString != null) {
            videoUri = Uri.parse(uriString)
        }

        setupPlayer()
        setupUI()
        videoUri?.let { uri ->
            generateThumbnails(uri)
        }
        startProgress()
    }

    fun View.expandTouchArea(size: Int) {

        val parentView = parent as View

        parentView.post {

            val rect = Rect()
            getHitRect(rect)

            rect.top -= size
            rect.bottom += size
            rect.left -= size
            rect.right += size

            parentView.touchDelegate =
                TouchDelegate(rect, this)
        }
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

        }

        binding.playerView.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    binding.ivPlayPause.visibility = View.VISIBLE
                } else {
                    it.play()
                    binding.ivPlayPause.visibility = View.GONE
                }
            }
        }

        binding.leftHandle.setOnTouchListener { v, event ->

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
                                binding.leftHandle.width

                    if (newX > maxX) {
                        newX = maxX.toFloat()
                    }

                    // không vượt rightHandle
                    val maxLeftX =
                        binding.rightHandle.x -
                                binding.leftHandle.width

                    if (newX > maxLeftX) {
                        newX = maxLeftX
                    }

                    binding.leftHandle.x = newX
                    binding.overlayLeft.layoutParams.width = binding.leftHandle.x.toInt()
                    binding.trimFrame.requestLayout()
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
                    }
                }
            }

            true
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        
        // Loop video
        player?.repeatMode = Player.REPEAT_MODE_ALL

        videoUri?.let {
            val mediaItem = MediaItem.fromUri(it)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
        } ?: run {
            // Placeholder nếu không có video truyền vào để demo giao diện
            Toast.makeText(this, "Không tìm thấy video để chỉnh sửa", Toast.LENGTH_SHORT).show()
        }
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
                }

                handler.postDelayed(this, 16)
            }
        })
    }

    private fun updatePlayHead() {

        val duration = player?.duration ?: 0L

        if (duration <= 0) return

        val current = player?.currentPosition ?: 0L

        val percent =
            current.toFloat() / duration

        // vùng timeline có thể chạy
        val startX =
            binding.leftHandle.x +
                    binding.leftHandle.width

        val endX =
            binding.rightHandle.x

        val availableWidth =
            endX - startX

        // playhead position
        var playHeadX =
            startX + (availableWidth * percent)

        // giới hạn trái
        if (playHeadX < startX) {
            playHeadX = startX
        }

        // giới hạn phải
        if (playHeadX > endX) {
            playHeadX = endX
        }

        binding.playHead.x = playHeadX
    }

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

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
