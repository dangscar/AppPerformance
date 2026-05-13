package com.nlhd.appperformance

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
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
import com.nlhd.appperformance.Activity.ProfileActivity
import com.nlhd.appperformance.Activity.SearchActivity
import com.nlhd.appperformance.Adapter.LoadingAdapter
import com.nlhd.appperformance.Adapter.VideoPagerAdapter
import com.nlhd.appperformance.BottomSheet.BottomSheetInputComment
import com.nlhd.appperformance.BottomSheet.BottomSheetShare
import com.nlhd.appperformance.BottomSheet.CommentBottomSheet
import com.nlhd.appperformance.ViewModel.CommentViewModel
import com.nlhd.appperformance.ViewModel.SearchSuccessViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DetailVideoActivity : AppCompatActivity() {
    private lateinit var adapter: VideoPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var searchContainer: LinearLayout
    private lateinit var ivBack: ImageView
    private lateinit var bottomComment: CardView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var loadingView: LottieAnimationView
    private lateinit var errorButton: Button
    private lateinit var playerView: PlayerView
    private lateinit var edtSearch: EditText
    private lateinit var edtComment: EditText

    private val viewModel: SearchSuccessViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()

    @Inject
    lateinit var players: MutableMap<Int, ExoPlayer>

    @Inject
    lateinit var defaultMediaSourceFactory: DefaultMediaSourceFactory

    private var isOnPageSelected = false
    private var currentCommentVideoId: String = "-1"
    private var statusBarHeight = 0

    private lateinit var commentBottomSheet: CommentBottomSheet
    val bottomSheetInput = BottomSheetInputComment(text = "",onChangeText = {}, onDone = {})

    @SuppressLint("MissingInflatedId")
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_video)
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            v.setPadding(maxOf(systemBars.left, cutout.left), systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

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
        edtSearch = findViewById(R.id.edtSearch)
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
                player.stop()
                player.clearMediaItems()
                player.release()
            }
            players.clear()
        }

        //Khi vào list video
        viewPager.post {
            val position = intent.getIntExtra("position", 0)
            if (viewModel.isCurrentItem.value == false) {
                viewPager.setCurrentItem(position, false)
                viewModel.setCurrentItem(true)
            }
        }



        adapter = VideoPagerAdapter(
            this,
            defaultMediaSourceFactory,
            players,
            onClickComment = { videoId->
                if (videoId == currentCommentVideoId.toInt()) {
                    commentBottomSheet.show(
                        supportFragmentManager,
                        CommentBottomSheet::class.java.simpleName
                    )
                } else {
                    currentCommentVideoId = videoId.toString()
                    //BottomSheet
                    commentBottomSheet = CommentBottomSheet(
                        viewModel = commentViewModel,
                        videoId = videoId.toString(),
                        onChangeBottomSheet = { width, height, offsetY ->
                            viewPager.post {
                                val currentPosition = viewModel.currentPosition.value ?: 0
                                val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder
                                if (holder == null) return@post
                                val density = holder.binding.playerView.resources.displayMetrics.density
                                val offset = offsetY   // [-1 .. 0]

                                val aspectRatio = holder.binding.playerView.width.toFloat() / holder.binding.playerView.height.toFloat()
                                val maxScale = 1f
                                val minScale = 0.16f + (aspectRatio * 0.8f)

                                val screenHeight= resources.configuration.screenHeightDp
                                val sheetHeight = height / density
                                val sheetVisible =   (sheetHeight / screenHeight)
                                val targetScale = (maxScale - (sheetVisible.coerceAtMost(0.6f) / 0.6f) * (maxScale - minScale))
                                val progress = (1f + offset).coerceIn(0f, 1f)
                                val scale = 1f - (1f - minScale) * progress

                                holder.binding.playerView.apply {
                                    pivotY = width / 3f
                                    scaleX = scale
                                    scaleY = scale

                                    val delta = height - height * scale
                                    translationY = -delta / 2f + statusBarHeight * progress * 0.25f
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }

                                if (offset != -1f) {
                                    toolbar.alpha = 0f
                                    bottomComment.alpha = 0f
                                    holder!!.binding.apply {
                                        actionColumn.alpha = 0f
                                        bottomInfo.alpha = 0f
                                        llBottomAction.alpha = 0f
                                    }
                                    window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
                                    window.statusBarColor = Color.BLACK
                                }
                            }
                        },
                        onDismiss = {

                            val currentPosition = viewModel.currentPosition.value ?: 0
                            val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder
                            toolbar.alpha = 1f
                            bottomComment.alpha = 1f
                            holder!!.binding.apply {
                                actionColumn.alpha = 1f
                                bottomInfo.alpha = 1f
                                llBottomAction.alpha = 1f
                            }
                            window.navigationBarColor = Color.BLACK
                            window.statusBarColor = Color.TRANSPARENT
                        },
                        onChangeComponent = {}
                    )
                }

            },
            onClickLike = { videoId, position->

            },
            onClickShare = {
                val bottomSheetShare = BottomSheetShare()
                bottomSheetShare.show(
                    supportFragmentManager,
                    BottomSheetShare::class.java.simpleName
                )
            },
            onClickProfile = {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }
        )

        observeViewModel()

        //Setup viewPager2
        if (!isLandscape) {
            playerView.visibility = View.GONE
            viewPager.adapter = adapter.withLoadStateFooter(
                footer = LoadingAdapter {adapter.retry() }
            )
            viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
            viewPager.offscreenPageLimit = 1
            adapter.resetAllPlayerExceptPos(viewModel.currentPosition.value ?: 0)
            viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position >= adapter.itemCount) return
                    if (!players.contains(position)) adapter.createPlayer(position)
                    val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position) as? VideoPagerAdapter.VideoViewHolder
                    val currentPosition = viewModel.currentPosition.value ?: 0
                    val player = players[position]
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
                }
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    val currentPosition = viewModel.currentPosition.value ?: 0
                    val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder
                    if (holder == null) return
                    holder.binding.apply {
                        if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                            actionColumn.alpha = 0.4f
                            bottomInfo.alpha = 0.4f
                            llBottomAction.alpha = 0.4f
                            holder.binding.seekBar.visibility = View.INVISIBLE
                        }
                        else if(state == ViewPager2.SCROLL_STATE_SETTLING){
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

            searchContainer.setOnClickListener {
                Intent(this, SearchActivity::class.java).apply {
                    startActivity(this)
                }
            }


        } else {
            val currentPosition = viewModel.currentPosition.value ?: 0
            val player = players[currentPosition]
            playerView.visibility = View.VISIBLE
            playerView.controllerShowTimeoutMs = 2500
            playerView.player = player
            player?.playWhenReady = true
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
            bottomSheetInput.show(
                supportFragmentManager,
                BottomSheetInputComment::class.java.simpleName
            )
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

        edtSearch.setOnClickListener {
            Intent(this, SearchActivity::class.java).apply {
                startActivity(this)
            }
        }
        edtComment.setOnClickListener {
            bottomSheetInput.show(
                supportFragmentManager,
                BottomSheetInputComment::class.java.simpleName
            )
        }

    }

    @OptIn(UnstableApi::class)
    private fun observeViewModel() {
        val keyword = intent.getStringExtra("keyword") ?: ""
        val type = intent.getStringExtra("type") ?: ""
        val timestamp = intent.getLongExtra("timestamp", 0L)
        when (type) {
            "explore" -> {
                lifecycleScope.launch {
                    viewModel.videosExplore.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
            }
            else -> {
                lifecycleScope.launch {
                    viewModel.videos(keyword, timestamp).collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val position = viewModel.currentPosition.value ?: 0
        if (!isLandscape) {
            adapter.pause(position)
        }

    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        val currentPosition = viewModel.currentPosition.value ?: 0
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) return
        if (isOnPageSelected) {
            adapter.createPlayer(currentPosition)
            val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(viewModel.currentPosition.value ?: 0) as? VideoPagerAdapter.VideoViewHolder ?: return
            holder.binding.playerView.player = players[viewModel.currentPosition.value ?: 0]
            adapter.handlePlayerState(viewModel.currentPosition.value ?: 0)
            adapter.setupTimeBar(holder, players[viewModel.currentPosition.value ?: 0]!!)
        }
        adapter.play(currentPosition)
    }


}