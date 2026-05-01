package com.nlhd.appperformance.Fragment

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.annotation.OptIn
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
import com.nlhd.appperformance.Activity.ProfileActivity
import com.nlhd.appperformance.Adapter.LoadingAdapter
import com.nlhd.appperformance.Adapter.VideoPagerAdapter
import com.nlhd.appperformance.BottomSheet.BottomSheetShare
import com.nlhd.appperformance.BottomSheet.CommentBottomSheet
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Feature.LoginScreen.LoginBottomSheet
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.Utils.TabSelected
import com.nlhd.appperformance.ViewModel.CommentViewModel
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.ViewModel.VideoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FollowingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class FollowingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var loadingView: LottieAnimationView
    private lateinit var ll_error: LinearLayout
    private lateinit var errorButton: Button
    private lateinit var playerView: PlayerView
    private lateinit var adapter: VideoPagerAdapter

    private val viewModel: VideoViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var isOnPageSelected = false

    var players: MutableMap<Int, ExoPlayer> = mutableMapOf()

    @Inject
    lateinit var defaultMediaSourceFactory: DefaultMediaSourceFactory
    private lateinit var commentBottomSheet: CommentBottomSheet

    private lateinit var btnPlay: ImageView
    private lateinit var btnPause: ImageView
    private var isLoaded = false

    private val registerOnPageChangeCallback = object :
        ViewPager2.OnPageChangeCallback() {
        @OptIn(UnstableApi::class)
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (position >= adapter.itemCount) return
            val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position) as? VideoPagerAdapter.VideoViewHolder
            adapter.createPlayer(position)
            if (holder != null) {
                viewPager.post {
                    val player = players[position]
                    holder.binding.playerView.player = player
                    adapter.setupTimeBar(holder, players[position]!!)
                }
            }
            isOnPageSelected = true
            viewModel.setCurrentPosition(position)
            //Được phát nếu giao diện là màu đen
            mainViewModel.tabSelected.observe(viewLifecycleOwner) {
                if (it == TabSelected.Following) {
                    adapter.handlePlayerState(position)
                }
            }
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
                    seekBar.alpha = 0.4f
                } else {
                    actionColumn.alpha = 1f
                    bottomInfo.alpha = 1f
                    llBottomAction.alpha = 1f
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_following, container, false)
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.viewPager)
        loadingView = view.findViewById(R.id.loadingView)
        ll_error = view.findViewById(R.id.ll_error)
        errorButton = view.findViewById(R.id.errorView)
        playerView = view.findViewById(R.id.playerViewlc)

        btnPlay = playerView.findViewById(R.id.exo_play)
        btnPause = playerView.findViewById(R.id.exo_pause)
        val btnBack = playerView.findViewById<ImageView>(R.id.exo_back)

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        //BottomSheet
        commentBottomSheet = CommentBottomSheet(
            viewModel = commentViewModel,
            onChangeBottomSheet = { width, height, offsetY ->
                val currentPosition = viewModel.currentPosition.value ?: 0
                val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder
                val offset = offsetY   // [-1 .. 0]
                if (holder != null) {
                    val aspectRatio = holder.binding.playerView.width.toFloat() / holder.binding.playerView.height.toFloat()
                    val minScale = 0.12f + (aspectRatio * 0.8f)
                    val progress = (1f + offset).coerceIn(0f, 1f)
                    val scale = 1f - (1f - minScale) * progress
                    val delta = height - height * scale

                    holder.binding.playerView.apply {
                        pivotY = width / 3f
                        scaleX = scale
                        scaleY = scale
                        translationY = -delta/2f
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }

                    if (offset != -1f) {
                        mainViewModel.showBarAction(false)
                        holder.binding.apply {
                            actionColumn.alpha = 0f
                            bottomInfo.alpha = 0f
                            llBottomAction.alpha = 0f
                        }
                    }
                }
            },
            onDismiss = {
                val currentPosition = viewModel.currentPosition.value ?: 0
                val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder
                mainViewModel.showBarAction(true)
                if (holder == null) return@CommentBottomSheet
                holder.binding.apply {
                    actionColumn.alpha = 1f
                    bottomInfo.alpha = 1f
                    llBottomAction.alpha = 1f
                }
            },
            onChangeComponent = {}
        )

        //Adapter
        adapter = VideoPagerAdapter(
            this.requireContext(),
            defaultMediaSourceFactory,
            players = players,
            onClickComment = {
                commentBottomSheet.show(
                    requireActivity().supportFragmentManager,
                    CommentBottomSheet::class.java.simpleName
                )
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
            }
        )

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

                    val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position) as? VideoPagerAdapter.VideoViewHolder
                    if (holder != null && video != null) {
                        adapter.updateLike(holder, video)
                    }
                    viewModel.updateStateLike()
                }
            }
        }
        //Nếu đang ở trạng thái nằm dọc
        if (!isLandscape) {
            playerView.visibility = View.GONE
            adapter.resetAllPlayerExceptPos(viewModel.currentPosition.value ?: 0)
            adapter.addLoadStateListener { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                val isError = loadStates.refresh is LoadState.Error

                loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
                ll_error.visibility = if (isError) View.VISIBLE else View.GONE
            }
            viewPager.adapter = adapter.withLoadStateFooter(
                footer = LoadingAdapter { adapter.retry() }
            )
            errorButton.setOnClickListener {
                adapter.retry()
            }
        }



    }

    @OptIn(UnstableApi::class)
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.userState.collect { userPreference ->
                if (userPreference.isLoggedIn && userPreference.token.isNotEmpty()) {
                    viewModel.videosFollowing(userPreference.token).collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        //mainViewModel.showBarAction(!isLandscape)
        mainViewModel.setLandscape(isLandscape)
        if (!isLandscape) {
            playerView.visibility = View.GONE
            viewPager.apply {
                //offscreenPageLimit = 1
                registerOnPageChangeCallback(registerOnPageChangeCallback)
            }
        } else {
            val currentPosition = viewModel.currentPosition.value ?: 0
            val player = players[currentPosition]
            playerView.visibility = View.VISIBLE
            errorButton.visibility = View.GONE
            loadingView.visibility = View.GONE
            playerView.player = player
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
            if (it == Navigation.Profile) {
                adapter.pause(viewModel.currentPosition.value ?: 0)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        players.values.forEach { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        players.clear()
    }


    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        if (!isLoaded) {
            observeViewModel()
            isLoaded = true
        }
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) return
        val currentPosition = viewModel.currentPosition.value ?: 0
        if (adapter.itemCount > 0) {
            adapter.createPlayer(currentPosition)
            val holder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPosition) as? VideoPagerAdapter.VideoViewHolder ?: return
            holder.binding.playerView.player = players[currentPosition]

            //Được phát nếu giao diện là màu đen
            mainViewModel.tabSelected.observe(viewLifecycleOwner) {
                if (it == TabSelected.Following && mainViewModel.navigation.value == Navigation.Home) {
                    adapter.handlePlayerState(currentPosition)
                }
            }
            adapter.setupTimeBar(holder, players[currentPosition]!!)
        }


        if (currentPosition+1 <adapter.itemCount) {
            adapter.createPlayer(currentPosition+1)
            val currentNext = (viewModel.currentPosition.value?.plus(1)) ?: 0
            val holderNext = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentNext) as? VideoPagerAdapter.VideoViewHolder ?: return
            holderNext.binding.playerView.player = players[currentNext]
        }

        if (currentPosition - 1 > -1) {
            adapter.createPlayer(currentPosition-1)
            val currentPrev = (viewModel.currentPosition.value?.minus(1)) ?: 0
            val holderPrev = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(currentPrev) as? VideoPagerAdapter.VideoViewHolder ?: return
            holderPrev.binding.playerView.player = players[currentPrev]
        }
    }

    override fun onStop() {
        super.onStop()
        viewPager.unregisterOnPageChangeCallback(registerOnPageChangeCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}