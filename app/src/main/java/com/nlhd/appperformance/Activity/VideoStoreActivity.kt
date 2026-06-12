package com.nlhd.appperformance.Activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.appbar.MaterialToolbar
import com.nlhd.appperformance.Adapter.LoadingAdapter
import com.nlhd.appperformance.Adapter.VideoStorePagerAdapter
import com.nlhd.appperformance.BottomSheet.BottomSheetInputComment
import com.nlhd.appperformance.BottomSheet.CommentBottomSheet
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.AudioEffects
import com.nlhd.appperformance.ViewModel.CommentViewModel
import com.nlhd.appperformance.ViewModel.VideoStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue
@UnstableApi
@AndroidEntryPoint
class VideoStoreActivity : AppCompatActivity() {
    private lateinit var adapter: VideoStorePagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var searchContainer: LinearLayout
    private lateinit var ivBack: ImageView
    private lateinit var bottomComment: CardView
    private lateinit var edtComment: EditText
    private lateinit var toolbar: MaterialToolbar
    private lateinit var loadingView: LottieAnimationView
    private lateinit var errorButton: Button
    private lateinit var playerView: PlayerView

    val viewModel: VideoStoreViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()

    @Inject
    lateinit var players: MutableMap<Int, ExoPlayer>
    @Inject
    lateinit var effectsMap: MutableMap<Int, AudioEffects>

    @Inject
    lateinit var defaultMediaSourceFactory: DefaultMediaSourceFactory

    private lateinit var commentBottomSheet: CommentBottomSheet

    private var isOnPageSelected = false
    private var statusBarHeight = 0
    private var isShow = false
    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_video_store)

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        //window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
//            v.setPadding(maxOf(systemBars.left, cutout.left), systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }


        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (isLandscape) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        toolbar = findViewById(R.id.topBar)
        viewPager = findViewById(R.id.viewPagerDetail)
        searchContainer = findViewById(R.id.searchContainer)
        ivBack = findViewById(R.id.ivBackDetail)
        bottomComment = findViewById(R.id.bottomBar)
        loadingView = findViewById(R.id.loadingView)
        errorButton = findViewById(R.id.errorView)
        playerView = findViewById(R.id.playerViewStore)
        edtComment = findViewById(R.id.edtComment)

        val btnPlay = playerView.findViewById<ImageView>(R.id.exo_play)
        val btnPause = playerView.findViewById<ImageView>(R.id.exo_pause)
        val btnBack = playerView.findViewById<ImageView>(R.id.exo_back)
        val tv_titlePv = playerView.findViewById<TextView>(R.id.tv_titlePv)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarHeight = systemBars.top

            toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            bottomComment.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            insets
        }

        bottomComment.visibility = if (isLandscape) View.GONE else View.VISIBLE
        searchContainer.visibility = if (isLandscape) View.GONE else View.VISIBLE
        ivBack.visibility = if (isLandscape) View.GONE else View.VISIBLE

        if (viewModel.isCurrentItem.value == false) {
            players.values.forEach { player ->
                releaseEffects(player)
                player.stop()
                player.clearMediaItems()
                player.release()
            }
            players.clear()
            effectsMap.clear()
        }

        //Khi vào list video
        viewPager.post {
            val position = intent.getIntExtra("position", 0)
            if (viewModel.isCurrentItem.value == false) {
                viewPager.setCurrentItem(position, false)
                viewModel.setCurrentItem(true)
            }
        }

        //BottomSheet
        commentBottomSheet = CommentBottomSheet(
            viewModel = commentViewModel,
            onChangeBottomSheet = { width, height, offsetY ->
                viewPager.post {
                    val currentPosition = viewModel.currentPosition.value ?: 0
                    val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoStorePagerAdapter.VideoViewHolder
                    if (holder == null) return@post
                    val offset = offsetY   // [-1 .. 0]
                    val aspectRatio = holder.binding.playerView.width.toFloat() / holder.binding.playerView.height.toFloat()
                    val minScale = 0.16f + (aspectRatio * 0.68f)
                    val progresses = (1f + offset).coerceIn(0f, 1f)
                    val scale = 1f - (1f - minScale) * progresses
                    val delta = height - height * scale

                    var translateY = -delta / 2f + statusBarHeight * progresses * 0.25f
                    var pivotY = width/4f
                    if (aspectRatio > 15/9f) {
                        translateY = -translateY
                        pivotY = width/1f
                    } else if (aspectRatio >= 1f) {
                        pivotY = width/1f
                        translateY += -delta * 2
                    } else if (aspectRatio > 9/16f) {
                        pivotY = width/1f
                        translateY += -delta * 1.5f * aspectRatio
                    }
                    holder.binding.playerView.apply {
                        this.pivotY = width/2f
                        scaleX = scale
                        scaleY = scale
                        translationY = -delta / 2f + statusBarHeight * progresses * 0.25f
                    }

                    if (offset != -1f) {
                        if (!isShow) {
                            toolbar.alpha = 0f
                            bottomComment.alpha = 0f
                            holder.binding.apply {
                                actionColumn.alpha = 0f
                                bottomInfo.alpha = 0f
                                llBottomAction.alpha = 0f
                            }
                        }

                        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
                        window.statusBarColor = Color.BLACK
                    }
                }
            },
            onDismiss = {
                window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
                window.statusBarColor = Color.TRANSPARENT

                val currentPosition = viewModel.currentPosition.value ?: 0
                val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoStorePagerAdapter.VideoViewHolder
                toolbar.alpha = 1f
                bottomComment.alpha = 1f
                holder!!.binding.apply {
                    actionColumn.alpha = 1f
                    bottomInfo.alpha = 1f
                    llBottomAction.alpha = 1f
                }
                isShow = false
            },
            onShow = {
                isShow = true
                val currentPosition = viewModel.currentPosition.value ?: 0
                val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoStorePagerAdapter.VideoViewHolder
                toolbar.alpha = 1f
                bottomComment.alpha = 1f
                holder!!.binding.apply {
                    actionColumn.alpha = 1f
                    bottomInfo.alpha = 1f
                    llBottomAction.alpha = 1f
                }
            },
            onChangeComponent = {}
        )

        //Adapter
        adapter = VideoStorePagerAdapter(
            this,
            defaultMediaSourceFactory,
            players = players,
            effectsMap = effectsMap,
            onClickComment = {
                commentBottomSheet.show(
                    supportFragmentManager,
                    CommentBottomSheet::class.java.simpleName
                )
            }
        )

        //Lấy dữ liệu paging adapter từ viewmodel
        observeViewModel()

        //Setup viewPager2
        if (!isLandscape) {
            playerView.visibility = View.GONE
            viewPager.adapter = adapter.withLoadStateFooter(
                footer = LoadingAdapter {adapter.retry() }
            )
            viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter.resetAllPlayerExceptPos(viewModel.currentPosition.value ?: 0)
            viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position >= adapter.itemCount) return
                    if (!players.contains(position)) adapter.createPlayer(position)
                    val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position) as? VideoStorePagerAdapter.VideoViewHolder
                    val player = players[position]
                    val currentPosition = viewModel.currentPosition.value ?: 0
                    if (holder != null && player != null) {
                        holder.binding.playerView.player = player
                        adapter.setupTimeBar(holder, player)
                    }
                    if (position != currentPosition) {
                        adapter.seekToStart(position)
                    }
                    isOnPageSelected = true
                    viewModel.setCurrentPosition(position)
                    adapter.handlePlayerState(position)

                    //Lấy thông tin video
                    val info = adapter.getViewPage(position)
                    viewModel.setInfoVideo(info)

                    adapter.updateCurrentPosition(position)

                    /*val runtime = Runtime.getRuntime()

                    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
                    val maxMemory = runtime.maxMemory() / 1024 / 1024
                    val totalMemory = runtime.totalMemory() / 1024 / 1024

                    Log.d("AAA", "Used: ${usedMemory}MB")
                    Log.d("AAA", "Total: ${totalMemory}MB")
                    Log.d("AAA", "Max: ${maxMemory}MB")*/
                }
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    val currentPosition = viewModel.currentPosition.value ?: 0
                    val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoStorePagerAdapter.VideoViewHolder
                    if (holder == null) return
                    holder.binding.apply {
                        if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                            actionColumn.alpha = 0.4f
                            bottomInfo.alpha = 0.4f
                            llBottomAction.alpha = 0.4f
                            holder.binding.seekBar.visibility = View.INVISIBLE
                        }
                        else if (state == ViewPager2.SCROLL_STATE_SETTLING) {
                            actionColumn.alpha = 1f
                            bottomInfo.alpha = 1f
                            llBottomAction.alpha = 1f
                            holder.binding.seekBar.visibility = View.INVISIBLE
                        }
                        else {
                            actionColumn.alpha = 1f
                            bottomInfo.alpha = 1f
                            llBottomAction.alpha = 1f
                            holder.binding.seekBar.visibility = View.VISIBLE
                        }
                    }
                }
            })

            val recyclerView = viewPager.getChildAt(0) as RecyclerView
            recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
            recyclerView.clipToPadding = true

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val pageWidth = viewPager.width
                    val currentOffset = rv.computeHorizontalScrollOffset()
                    val currentPage = viewPager.currentItem

                    val minOffset = (currentPage - 1) * pageWidth
                    val maxOffset = (currentPage + 1) * pageWidth

                    if (currentOffset < minOffset) {
                        rv.scrollBy(minOffset - currentOffset, 0)
                    } else if (currentOffset > maxOffset) {
                        rv.scrollBy(maxOffset - currentOffset, 0)
                    }
                }
            })

            adapter.addLoadStateListener { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                val isError = loadStates.refresh is LoadState.Error
                loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
                errorButton.visibility = if (isError) View.VISIBLE else View.GONE
            }

            errorButton.setOnClickListener {
                adapter.retry()
            }

        } else {
            val currentPosition = viewModel.currentPosition.value ?: 0
            val player = players[currentPosition]
            playerView.visibility = View.VISIBLE
            playerView.controllerShowTimeoutMs = 2500
            playerView.player = player
            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying) {
                        btnPlay.visibility = View.GONE
                        btnPause.visibility = View.VISIBLE
                    } else {
                        btnPlay.visibility = View.VISIBLE
                        btnPause.visibility = View.GONE
                    }
                }
            })

            btnPlay.setOnClickListener {
                player?.play()
            }
            btnPause.setOnClickListener {
                player?.pause()
            }
            viewModel.infoVideo.observe(this) {
                tv_titlePv.text = it
            }
            /*btnBack.setOnClickListener {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }*/
        }

        //Click bottomSheetComment
        bottomComment.setOnClickListener {
            BottomSheetInputComment(text = "", onChangeText = {}, onDone = {}, imageUrl = "http://192.168.1.168/Shop/public/images/user.jpeg").show(supportFragmentManager, BottomSheetInputComment::class.java.simpleName)
        }
        edtComment.setOnClickListener {
            BottomSheetInputComment(text = "", onChangeText = {}, onDone = {}, imageUrl = "http://192.168.1.168/Shop/public/images/user.jpeg").show(supportFragmentManager, BottomSheetInputComment::class.java.simpleName)
        }

        //Nút back
        ivBack.setOnClickListener {
            if (::adapter.isInitialized) {
                adapter.releaseAllPlayers()
            }
            finish()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        //Nút backPress
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::adapter.isInitialized) {
                    adapter.releaseAllPlayers()
                }
                finish()
                overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }

        })
    }
    @OptIn(UnstableApi::class)
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.videoStore.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }
    override fun onPause() {
        super.onPause()
        val currentPosition = viewModel.currentPosition.value ?: 0
        adapter.pause(currentPosition)
    }
    override fun onStart() {
        super.onStart()
        val currentPosition = viewModel.currentPosition.value ?: 0
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isOnPageSelected && !isLandscape) {
            adapter.createPlayer(currentPosition)
            val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(viewModel.currentPosition.value ?: 0) as? VideoStorePagerAdapter.VideoViewHolder ?: return
            holder.binding.playerView.player = players[viewModel.currentPosition.value ?: 0]
            adapter.handlePlayerState(viewModel.currentPosition.value ?: 0)
            adapter.setupTimeBar(holder, players[viewModel.currentPosition.value ?: 0]!!)
        }
        adapter.play(currentPosition)
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