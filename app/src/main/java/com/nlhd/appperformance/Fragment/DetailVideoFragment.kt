package com.nlhd.appperformance.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
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
import com.nlhd.appperformance.DetailVideoActivity
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Feature.Video.onChangeBottomSheet
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Follow
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.Utils.TabSelected
import com.nlhd.appperformance.ViewModel.CommentViewModel
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.ViewModel.SearchSuccessViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class DetailVideoFragment : Fragment() {
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
    private val commentViewModel: CommentViewModel by viewModels()
    private val viewModel: SearchSuccessViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var players: MutableMap<Int, ExoPlayer>

    @Inject
    lateinit var defaultMediaSourceFactory: DefaultMediaSourceFactory
    private var currentCommentVideoId: String = "-1"
    private var statusBarHeight = 0


    private lateinit var commentBottomSheet: CommentBottomSheet

    @OptIn(UnstableApi::class)
    fun holder(position: Int) = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position) as? VideoPagerAdapter.VideoViewHolder
    fun player(position: Int) = players[position]

    @OptIn(UnstableApi::class)
    fun setPlayerView(holder: VideoPagerAdapter.VideoViewHolder, player: ExoPlayer) {
        holder.binding.playerView.player = player
        adapter.setupTimeBar(holder, player)
    }

    @OptIn(UnstableApi::class)
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
            if (mainViewModel.navigation.value == Navigation.Home) {
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
    @OptIn(UnstableApi::class)
    fun layoutAlpha(holder: VideoPagerAdapter.VideoViewHolder, value: Float) {
        holder.binding.apply {
            actionColumn.alpha = value
            bottomInfo.alpha = value
            llBottomAction.alpha = value
        }
    }

    private val registerOnPageChangeCallback = object :
        ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (position >= adapter.itemCount) return
            setUpPlayer(position)
        }

        @OptIn(UnstableApi::class)
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)

            val position = viewPager.currentItem
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

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_video, container, false)
    }

    @SuppressLint("MissingInflatedId")
    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        toolbar = view.findViewById(R.id.topBar)
        viewPager = view.findViewById(R.id.viewPagerDetail)
        searchContainer = view.findViewById(R.id.searchContainer)
        ivBack = view.findViewById(R.id.ivBackDetail)
        bottomComment = view.findViewById(R.id.bottomBar)
        loadingView = view.findViewById(R.id.loadingView)
        errorButton = view.findViewById(R.id.errorView)
        playerView = view.findViewById(R.id.playerViewStore)
        edtSearch = view.findViewById(R.id.edtSearch)
        edtComment = view.findViewById(R.id.edtComment)

        val btnPlay = playerView.findViewById<ImageView>(R.id.exo_play)
        val btnPause = playerView.findViewById<ImageView>(R.id.exo_pause)
        val tv_titlePv = playerView.findViewById<TextView>(R.id.tv_titlePv)

//        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            statusBarHeight = systemBars.top
//            toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                topMargin = systemBars.top
//            }
//            bottomComment.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                bottomMargin = systemBars.bottom
//            }
//            insets
//        }

        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())

            insets?.let {
                toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top
                }
                bottomComment.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = it.bottom
                }
            }
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

        viewPager.post {
            val position = activity?.intent?.getIntExtra("position", 0) ?: 0
            if (viewModel.isCurrentItem.value == false) {
                viewPager.setCurrentItem(position, false)
                viewModel.setCurrentItem(true)
            }
        }

        adapter = VideoPagerAdapter(
            requireContext(),
            defaultMediaSourceFactory,
            players,
            onClickComment = { videoId ->
                if (videoId == currentCommentVideoId.toInt()) {
                    commentBottomSheet.show(
                        childFragmentManager,
                        CommentBottomSheet::class.java.simpleName
                    )
                } else {
                    currentCommentVideoId = videoId.toString()
                    commentBottomSheet = CommentBottomSheet(
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
                                    toolbar.alpha = 0f
                                    bottomComment.alpha = 0f
                                    holder.binding.apply {
                                        actionColumn.alpha = 0f
                                        bottomInfo.alpha = 0f
                                        llBottomAction.alpha = 0f
                                    }
                                    activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)
                                    activity?.window?.statusBarColor = Color.BLACK
                                },
                                onShowSearchIcon = {}
                            )
                        },
                        onDismiss = {
                            val currentPosition = viewModel.currentPosition.value ?: 0
                            val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder
                            toolbar.alpha = 1f
                            bottomComment.alpha = 1f
                            holder?.binding?.apply {
                                actionColumn.alpha = 1f
                                bottomInfo.alpha = 1f
                                llBottomAction.alpha = 1f
                            }
                            activity?.window?.navigationBarColor = Color.BLACK
                            activity?.window?.statusBarColor = Color.TRANSPARENT
                        },
                        onChangeComponent = {}
                    )
                    commentBottomSheet.show(childFragmentManager, CommentBottomSheet::class.java.simpleName)
                }
            },
            onClickLike = { videoId, position -> },
            onClickShare = {
                val bottomSheetShare = BottomSheetShare()
                bottomSheetShare.show(childFragmentManager, BottomSheetShare::class.java.simpleName)
            },
            onClickProfile = {
                // Here we might want to tell the activity to switch to ProfileFragment
                val isUserInputEnable = activity?.intent?.getBooleanExtra("isUserInputEnable", true)
                if (isUserInputEnable == false) return@VideoPagerAdapter
                (activity as? DetailVideoActivity)?.switchToProfile()
            },
            onClickFollow = {

            }
        )

        observeViewModel()

        if (!isLandscape) {
            playerView.visibility = View.GONE
            viewPager.adapter = adapter.withLoadStateFooter(
                footer = LoadingAdapter { adapter.retry() }
            )
            viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter.resetAllPlayerExceptPos(viewModel.currentPosition.value ?: 0)
            viewPager.apply {
                registerOnPageChangeCallback(registerOnPageChangeCallback)
            }

            val recyclerView = viewPager.getChildAt(0) as RecyclerView
            recyclerView.overScrollMode = View.OVER_SCROLL_NEVER

            adapter.addLoadStateListener { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                val isError = loadStates.refresh is LoadState.Error
                loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
                errorButton.visibility = if (isError) View.VISIBLE else View.GONE
            }

            errorButton.setOnClickListener { adapter.retry() }
            searchContainer.setOnClickListener {
                Intent(requireContext(), SearchActivity::class.java).apply { startActivity(this) }
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

            btnPlay.setOnClickListener { player?.play() }
            btnPause.setOnClickListener { player?.pause() }
            viewModel.infoVideo.observe(viewLifecycleOwner) { tv_titlePv.text = it }
        }

        bottomComment.setOnClickListener {
            BottomSheetInputComment(text = "", onChangeText = {}, onDone = {}).show(childFragmentManager, BottomSheetInputComment::class.java.simpleName)
        }

        ivBack.setOnClickListener {
            if (::adapter.isInitialized) {
                adapter.releaseAllPlayers()
            }
            activity?.finish()
            activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        edtSearch.setOnClickListener {
            Intent(requireContext(), SearchActivity::class.java).apply { startActivity(this) }
        }
        edtComment.setOnClickListener {
            BottomSheetInputComment(text = "", onChangeText = {}, onDone = {}).show(childFragmentManager, BottomSheetInputComment::class.java.simpleName)
        }

        mainViewModel.navigation.observe(viewLifecycleOwner) {
            if (it == Navigation.Home && viewModel.currentPosition.value == viewPager.currentItem) {
                adapter.handlePlayerState(viewModel.currentPosition.value ?: 0)
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

    }

    @OptIn(UnstableApi::class)
    private fun observeViewModel() {
        val keyword = activity?.intent?.getStringExtra("keyword") ?: ""
        val type = activity?.intent?.getStringExtra("type") ?: ""
        val timestamp = activity?.intent?.getLongExtra("timestamp", 0L) ?: 0L
        when (type) {
            "explore" -> {
                lifecycleScope.launch {
                    viewModel.videosExplore.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
            }
            "profile" -> {
                val timeProfile = activity?.intent?.getLongExtra("timestamp", 0L) ?: 0L
                val userId = activity?.intent?.getStringExtra("userId") ?: "0"
                val token = activity?.intent?.getStringExtra("token") ?: ""
                lifecycleScope.launch {
                    viewModel.videosProfile(userId, timeProfile, token).collectLatest { pagingData ->
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
        if (!isLandscape && ::adapter.isInitialized) {
            adapter.pause(position)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) return
        val currentPosition = viewPager.currentItem

        if (adapter.itemCount > 0) {
            adapter.createPlayer(currentPosition)
            val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder ?: return
            holder.binding.playerView.player = players[currentPosition]
            mainViewModel.tabSelected.observe(viewLifecycleOwner) {
                if (mainViewModel.navigation.value == Navigation.Home) {
                    adapter.handlePlayerState(viewPager.currentItem)
                }
            }
            val player = players[currentPosition]
            if (player != null) adapter.setupTimeBar(holder, player)
        }


        val currentNext = currentPosition + 1
        if (currentNext <adapter.itemCount) {
            adapter.createPlayer(currentNext)
            val player = players[currentNext]
            val holderNext = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentNext) as? VideoPagerAdapter.VideoViewHolder ?: return
            holderNext.binding.playerView.player = player
        }

        val currentPrev = currentPosition - 1
        if (currentPrev > -1) {
            adapter.createPlayer(currentPrev)
            val holderPrev = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPrev) as? VideoPagerAdapter.VideoViewHolder ?: return
            holderPrev.binding.playerView.player = players[currentPrev]
        }
    }

    fun releasePlayers() {
        if (::adapter.isInitialized) {
            adapter.releaseAllPlayers()
        }
    }


}