package com.nlhd.appperformance.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nlhd.appperformance.Activity.ProfileActivity
import com.nlhd.appperformance.Activity.SearchActivity
import com.nlhd.appperformance.Adapter.LoadingAdapter
import com.nlhd.appperformance.Adapter.VideoPagerAdapter
import com.nlhd.appperformance.Adapter.VideoPagerAdapter.Companion.PAYLOAD_FOLLOW
import com.nlhd.appperformance.BottomSheet.BottomSheetShare
import com.nlhd.appperformance.BottomSheet.CommentBottomSheet
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Feature.Video.onChangeBottomSheet
import com.nlhd.appperformance.R
import com.nlhd.appperformance.ThuNghiem.TwoFingerScrollHelper
import com.nlhd.appperformance.Utils.Follow
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.Utils.TabSelected
import com.nlhd.appperformance.ViewModel.CommentViewModel
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.ViewModel.VideoViewModel
import com.nlhd.appperformance.databinding.FragmentForyouBinding
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@UnstableApi
@AndroidEntryPoint
class ForYouFragment(
) : Fragment() {

    private var _binding: FragmentForyouBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VideoPagerAdapter

    private val commentViewModel: CommentViewModel by viewModels()
    private val viewModel: VideoViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var players: MutableMap<Int, ExoPlayer>

    @Inject
    lateinit var defaultMediaSourceFactory: DefaultMediaSourceFactory

    private lateinit var btnPlay: ImageView
    private lateinit var btnPause: ImageView

    private var commentBottomSheet: CommentBottomSheet? = null
    private var currentCommentVideoId: String = "-1"
    private val ivSearchOverlay by lazy { binding.ivSearchOverlay }
    private var isClickBottomSheetComment = false
    private var statusBarHeight = 0
    private var currentOffset = 0f
    private var currentPositionV = 0
    private var isLock = false

    fun holder(position: Int) = (binding.viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position) as? VideoPagerAdapter.VideoViewHolder
    fun player(position: Int) = players[position]
    fun setPlayerView(holder: VideoPagerAdapter.VideoViewHolder, player: ExoPlayer) {
        holder.binding.playerView.player = player
        adapter.setupTimeBar(holder, player)
    }
    fun setUpPlayer(position: Int) {
        if (!players.contains(position)) adapter.createPlayer(position)
        val holder = holder(position)
        val player = player(position)
        if (holder != null && player != null) {
            setPlayerView(holder, player)
        }

        /* Thiết lập vị trí hiện tại không thay đổi*/
        viewModel.setCurrentPosition(position)

        //Được phát nếu giao diện là màu đen
        mainViewModel.tabSelected.observe(viewLifecycleOwner) {
            if (it == TabSelected.Suggestions && mainViewModel.navigation.value == Navigation.Home && position == binding.viewPager.currentItem) {
                adapter.handlePlayerState(position)
            }
        }


        //Cập nhật vị trí position
        adapter.updateCurrentPosition(position)

        //Set idProfile
        mainViewModel.setIdProfile(adapter.videoByPosition(position)?.userId?.toInt() ?: -1)

        ///set state follow
        val video = adapter.videoByPosition(position)
        when (video?.canFollow) {
            "1" -> {
                if (video.isFollowing == "0") { //Chưa follow
                    mainViewModel.setFollowState(Follow.NOT_FOLLOW)
                } else { //Đã follow
                    mainViewModel.setFollowState(Follow.FOLLOWED)
                }
            }
            else -> { //Đối với profile
                mainViewModel.setFollowState(Follow.MY_PROFILE)
            }
        }
    }
    /* Thay đổi màu alpha của layout*/
    fun layoutAlpha(holder: VideoPagerAdapter.VideoViewHolder, value: Float) {
        holder.binding.apply {
            actionColumn.alpha = value
            bottomInfo.alpha = value
            llBottomAction.alpha = value
        }
    }
    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
    fun inset(view: View) {
        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())

            insets?.let {
                statusBarHeight = it.top
                ivSearchOverlay.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top + 12.dpToPx()  // ✅ status bar + margin thêm
                    marginEnd = 14.dpToPx()
                }
                binding.ivAutoScroll.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top + 50.dpToPx()  // ✅ status bar + margin thêm
                }
            }
        }
    }
    fun colorSystem(navigationColor: Int, statusBarColor: Int) {
        requireActivity().window.navigationBarColor = navigationColor
        requireActivity().window.statusBarColor = statusBarColor
    }
    private val registerOnPageChangeCallback = object :
        ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (position >= adapter.itemCount) return
            setUpPlayer(position)
        }
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)

            val position = binding.viewPager.currentItem
            val holder = holder(position) ?: return
            if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                layoutAlpha(holder, 0.4f)
                holder.binding.seekBar.visibility = View.INVISIBLE
            }
            else if (state == ViewPager2.SCROLL_STATE_SETTLING) {
                layoutAlpha(holder, 1f)
                holder.binding.seekBar.visibility = View.INVISIBLE
            }
            else {
                layoutAlpha(holder, 1f)
                holder.binding.seekBar.visibility = View.VISIBLE
            }
        }
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            currentOffset = positionOffset
            currentPositionV = position

            val recyclerView = binding.viewPager.getChildAt(0) as RecyclerView
            val pageHeight = recyclerView.height

            // % kéo thực tế theo pixel
            val progress =
                positionOffsetPixels.toFloat() / pageHeight.toFloat()
            if (viewModel.currentPosition.value == position && progress <= 0.95f) {
                isLock = false
            }
            else if (viewModel.currentPosition.value!!-1 == position && progress >= 0.05f) {
                isLock = false
            }
            else if (viewModel.currentPosition.value == position && positionOffset <= 0.95f) {
                isLock = false
            }
            else if (viewModel.currentPosition.value!!-1 == position &&positionOffset >= 0.05f){
                isLock = false
            }
            else {
                isLock = true
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForyouBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    @SuppressLint("ClickableViewAccessibility")
    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inset(view)
        colorSystem(navigationColor = Color.BLACK, statusBarColor = Color.TRANSPARENT)

        btnPlay = binding.playerViewlc.findViewById(R.id.exo_play)
        btnPause = binding.playerViewlc.findViewById(R.id.exo_pause)
        val btnBack = binding.playerViewlc.findViewById<ImageView>(R.id.exo_back)

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val position = viewModel.currentPosition.value ?: 0
        //Adapter
        adapter = VideoPagerAdapter(
            requireContext(),
            defaultMediaSourceFactory,
            players = players,
            onClickComment = { videoId->
                if (isClickBottomSheetComment) return@VideoPagerAdapter
                isClickBottomSheetComment = true
                //Nếu videoId hiện tại mà bằng với videoId trước đó thì mở bottomSheet
                if (videoId == currentCommentVideoId.toInt()) {
                    commentBottomSheet?.show(
                        requireActivity().supportFragmentManager,
                        CommentBottomSheet::class.java.simpleName
                    )
                } else { //Ngược lại tạo ra bottomSheet khác
                    currentCommentVideoId = videoId.toString()
                    commentBottomSheet =CommentBottomSheet(
                        viewModel = commentViewModel,
                        videoId = videoId.toString(),
                        onChangeBottomSheet = { width, height, offsetY ->
                            val holder = holder(viewModel.currentPosition.value ?: 0) ?: return@CommentBottomSheet
                            onChangeBottomSheet(
                                width,
                                height,
                                offsetY,
                                holder,
                                statusBarHeight,
                                onDoNotShow = {
                                    ivSearchOverlay.visibility = View.GONE
                                    mainViewModel.showBarAction(false)
                                    layoutAlpha(holder, 0f)
                                    requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
                                    requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)
                                },
                                onShowSearchIcon = {
                                    ivSearchOverlay.visibility = View.VISIBLE
                                }
                            )
                        },
                        onDismiss = {
                            //Dismiss bottomSheet
                            requireActivity().window.navigationBarColor =
                                ContextCompat.getColor(requireContext(), R.color.black)
                            requireActivity().window.statusBarColor = Color.TRANSPARENT

                            ivSearchOverlay.visibility = View.GONE

                            val currentPosition = viewModel.currentPosition.value ?: 0
                            val holder = holder(currentPosition)
                            mainViewModel.showBarAction(true)
                            if (holder == null) return@CommentBottomSheet
                            layoutAlpha(holder, 1f)

                            isClickBottomSheetComment = false

                        },
                        onChangeComponent = {
                            //Update commentCount
                            val currentPosition = viewModel.currentPosition.value ?: 0
                            val holder = (binding.viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder
                            val video = adapter.videoByPosition(currentPosition) ?: return@CommentBottomSheet
                            if (video.commentsCount.toInt() < it.commentCount) {
                                video.commentsCount = it.commentCount.toString()
                            }
                            holder?.binding?.txtComment?.text = video.commentsCount
                        }
                    )
                    commentBottomSheet?.show(
                        requireActivity().supportFragmentManager,
                        CommentBottomSheet::class.java.simpleName
                    )
                }


            },
            onClickLike = { videoId, position->
                lifecycleScope.launch {
                    viewModel.userState.collect { userPreference ->
                        if (userPreference.isLoggedIn && userPreference.token.isNotEmpty()) {
                            viewModel.like(userPreference.token, videoId.toString())
                        }
                    }
                }
            },
            onClickShare = {
                val bottomSheetShare = BottomSheetShare()
                bottomSheetShare.show(
                    requireActivity().supportFragmentManager,
                    BottomSheetShare::class.java.simpleName
                )
            },
            onClickProfile = {
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            },
            onClickFollow = { userId->
                lifecycleScope.launch {
                    viewModel.userState.collect { userPreference ->
                        if (userPreference.isLoggedIn && userPreference.token.isNotEmpty()) {
                            mainViewModel.follow(userPreference.token, userId.toString())
                        }
                    }
                }
            }
        )


        //Nếu đang ở trạng thái nằm dọc
        if (!isLandscape) {
            binding.playerViewlc.visibility = View.GONE
            adapter.resetAllPlayerExceptPos(position)
            adapter.addLoadStateListener { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                val isError = loadStates.refresh is LoadState.Error

                binding.loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.viewPager.visibility = if (isLoading) View.GONE else View.VISIBLE
                binding.llError.visibility = if (isError) View.VISIBLE else View.GONE
                if (loadStates.refresh is LoadState.NotLoading) {

                }
            }
            binding.viewPager.adapter = adapter.withLoadStateFooter(
                footer = LoadingAdapter { adapter.retry() }
            )
            binding.errorView.setOnClickListener {
                adapter.retry()
            }
            binding.viewPager.apply {
                registerOnPageChangeCallback(registerOnPageChangeCallback)
            }
            val recyclerView = binding.viewPager.getChildAt(0) as RecyclerView
            recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        }


        //Refresh trang
        mainViewModel.refresh.observe(viewLifecycleOwner) {
            if (it) {
                refreshData()
                mainViewModel.setRefresh(false)
            }
        }

        //Follow State
        mainViewModel.isFollowing.observe(viewLifecycleOwner) {
            when (it) {
                is ResultUI.Success<*> -> {

                    val message = it.data as MessageResponse

                    val currentPos = viewModel.currentPosition.value ?: return@observe

                    val video = adapter.videoByPosition(currentPos) ?: return@observe
                    // update data
                    video.isFollowing =
                        if (message.message == "Follow thành công") {
                            "1"
                        } else {
                            "0"
                        }
                    // update toàn bộ video cùng user
                    adapter.updateFollowByUserId(
                        userId = video.user.id,
                        isFollowing = video.isFollowing
                    )
                    if (video.isFollowing == "0") {
                        mainViewModel.setFollowState(Follow.NOT_FOLLOW)
                    } else {
                        mainViewModel.setFollowState(Follow.FOLLOWED)
                    }
                    mainViewModel.setFollowIdle()
                }
                else -> {}
            }
        }

        //Like State
        viewModel.likeState.observe(viewLifecycleOwner) {
            when (it) {
                is ResultUI.Error<*> -> {}
                ResultUI.Idle -> {}
                ResultUI.Loading -> {}
                is ResultUI.Success<*> -> {
                    val message = (it as ResultUI.Success).data
                    val position = viewModel.currentPosition.value ?: 0
                    val video = adapter.videoByPosition(position)
                    when (message.message) {
                        "Added" -> {
                            video?.isLiked =  1.toString()
                            video?.likesCount = video.likesCount.toInt().plus(1).toString()
                        }
                        "Deleted" -> {
                            video?.isLiked =  0.toString()
                            video?.likesCount = video.likesCount.toInt().minus(1).toString()
                        }
                    }

                    val holder = (binding.viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position) as? VideoPagerAdapter.VideoViewHolder
                    if (holder != null && video != null) {
                        adapter.updateLike(holder, video)
                    }
                    viewModel.updateStateLike()
                }
            }
        }


        /*var startY = 0f
        var lastDy = 0f

        binding.viewPager.getChildAt(0).setOnTouchListener { _, event ->

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    startY = event.rawY
                    lastDy = 0f
                }

                MotionEvent.ACTION_MOVE -> {

                    val dy = event.rawY - startY


                    // So sánh với frame trước
                    val isSwipeUp = dy > lastDy
                    val isSwipeDown = dy < lastDy

                    when {

                        isSwipeUp -> {
                            Log.d("AAA", "VUỐT LÊN")
                        }

                        isSwipeDown -> {
                            Log.d("AAA", "VUỐT XUỐNG")
                        }
                    }

                    // cập nhật frame trước
                    lastDy = dy

                    if (isLock) {
                        return@setOnTouchListener true
                    }
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    lastDy = 0f
                }
            }

            false
        }*/
    }

    var isRefreshing = false
    fun refreshData() {
        if (!::players.isInitialized) return
        isRefreshing = true
        releaseAllPlayers()
        binding.viewPager.setCurrentItem(0, false)
        adapter.refresh()

        binding.viewPager.doOnPreDraw {
            adapter.addLoadStateListener {
                if (it.refresh is LoadState.NotLoading && isRefreshing) {
                    releaseAllPlayers()
                    binding.viewPager.setCurrentItem(0, false)
                    initializePlayerForCurrentItem()
                    isRefreshing = false

                    val video = adapter.videoByPosition(viewModel.currentPosition.value ?: 0)
                    when (video?.canFollow) {
                        "1" -> {
                            if (video.isFollowing == "0") {
                                mainViewModel.setFollowState(Follow.NOT_FOLLOW)
                            } else {
                                mainViewModel.setFollowState(Follow.FOLLOWED)
                            }
                        }
                        else -> {
                            mainViewModel.setFollowState(Follow.MY_PROFILE)
                        }
                    }
                }
            }

        }
    }

    private fun releaseAllPlayers() {
        players.values.forEach { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        players.clear()
    }

    private fun initializePlayerForCurrentItem() {
        val currentIndex = 0 // = 0

        adapter.createPlayer(currentIndex)

        val recyclerView = binding.viewPager.getChildAt(0) as? RecyclerView ?: return
        val holder = recyclerView
            .findViewHolderForAdapterPosition(currentIndex) as? VideoPagerAdapter.VideoViewHolder
            ?: return

        val player = players[currentIndex] ?: return

        with(holder.binding.playerView) {
            this.player = player
        }

        adapter.setupTimeBar(holder, player)
        adapter.handlePlayerState(currentIndex)
    }

    @OptIn(UnstableApi::class)
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.videos.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val currentPosition = viewModel.currentPosition.value ?: 0

//        mainViewModel.showBarAction(!isLandscape)
        mainViewModel.setLandscape(isLandscape)
        observeViewModel()
        if (!isLandscape) {
            binding.playerViewlc.visibility = View.GONE
        } else {
            val player = players[currentPosition]
            binding.playerViewlc.visibility = View.VISIBLE
            binding.errorView.visibility = View.GONE
            binding.loadingView.visibility = View.GONE
            binding.playerViewlc.player = player
            player?.play()
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
        }

        mainViewModel.navigation.observe(viewLifecycleOwner) {
            if (it != Navigation.Home) {
                val position = viewModel.currentPosition.value ?: 0
                adapter.pause(position)
            } else {
                val isPlaying = mainViewModel.tabSelected.value == TabSelected.Suggestions
                if (isPlaying) {
                    val position = viewModel.currentPosition.value ?: 0
                    adapter.handlePlayerState(position)
                }
            }
        }

    }


    override fun onPause() {
        super.onPause()
        val currentPosition = viewModel.currentPosition.value ?: 0
        adapter.pause(currentPosition)
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) return
        val currentPosition = binding.viewPager.currentItem

        if (adapter.itemCount > 0) {
            adapter.createPlayer(currentPosition)
            val holder = (binding.viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder ?: return
            holder.binding.playerView.player = players[currentPosition]
            mainViewModel.tabSelected.observe(viewLifecycleOwner) {
                if (it == TabSelected.Suggestions && mainViewModel.navigation.value == Navigation.Home) {
                    adapter.handlePlayerState(binding.viewPager.currentItem)
                }
            }
            val player = players[currentPosition]
            if (player != null) adapter.setupTimeBar(holder, player)
        }


        val currentNext = currentPosition + 1
        if (currentNext <adapter.itemCount) {
            adapter.createPlayer(currentNext)
            val player = players[currentNext]
            val holderNext = (binding.viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentNext) as? VideoPagerAdapter.VideoViewHolder ?: return
            holderNext.binding.playerView.player = player
        }

        val currentPrev = currentPosition - 1
        if (currentPrev > -1) {
            adapter.createPlayer(currentPrev)
            val holderPrev = (binding.viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPrev) as? VideoPagerAdapter.VideoViewHolder ?: return
            holderPrev.binding.playerView.player = players[currentPrev]
        }
    }


}


