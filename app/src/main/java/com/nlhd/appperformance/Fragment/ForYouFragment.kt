package com.nlhd.appperformance.Fragment

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
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
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
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
import com.nlhd.appperformance.BottomSheet.BottomSheetShare
import com.nlhd.appperformance.BottomSheet.CommentBottomSheet
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.R
import com.nlhd.appperformance.ThuNghiem.TwoFingerScrollHelper
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

    private val viewModel: VideoViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()
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
        mainViewModel.setIdProfile(adapter.videoByPosition(position)?.id ?: -1)
    }

    /* Thay đổi màu alpha của layout*/
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForyouBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
    private var statusBarHeight = 0

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())

            insets?.let {
                statusBarHeight = it.top
                ivSearchOverlay.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top + 12.dpToPx()  // ✅ status bar + margin thêm
                    marginEnd = 14.dpToPx()
                }
            }
        }

        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.black)
        requireActivity().window.statusBarColor = Color.TRANSPARENT

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
                            val currentPosition = viewModel.currentPosition.value ?: 0
                            val holder = holder(currentPosition)
                            val offset = offsetY   // [-1 .. 0]
                            if (holder != null) {
                                val aspectRatio = holder.binding.playerView.width.toFloat() / holder.binding.playerView.height.toFloat()
                                val minScale = 0.16f + (aspectRatio * 0.8f)
                                val progress = (1f + offset).coerceIn(0f, 1f)
                                val scale = 1f - (1f - minScale) * progress
                                val delta = height - height * scale

                                holder.binding.playerView.apply {
                                    pivotY = width / 3f
                                    scaleX = scale
                                    scaleY = scale
                                    translationY = -delta / 2f + statusBarHeight * progress * 0.25f
                                }

                                if (offset != -1f) {
                                    mainViewModel.showBarAction(false)
                                    layoutAlpha(holder, 0f)
                                    requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
                                    requireActivity().window.navigationBarColor =
                                        ContextCompat.getColor(requireContext(), R.color.white)
                                }
                            }

                            val progress = (1f + offsetY).coerceIn(0f, 1f)

                            // Show icon khi bottomSheet mở (progress > 0)
                            if (progress > 0f && ivSearchOverlay.visibility != View.VISIBLE) {
                                ivSearchOverlay.visibility = View.VISIBLE
                                ivSearchOverlay.alpha = 0f
                            }
                            // Fade in/out theo progress
                            ivSearchOverlay.alpha = 1f
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
//            binding.btnRefresh.setOnClickListener {
//                adapter.refresh()
//                adapter.releaseAllPlayers()
//                binding.viewPager.setCurrentItem(0, false)
//                binding.viewPager.post {
//                    setUpPlayer(0)
//                    Log.d("AAA", binding.viewPager.currentItem.toString())
//                }
//            }

        }

        /*viewModel.isFollowing.observe(viewLifecycleOwner) {
            when (it) {
                is ResultUI.Error<*> -> {}
                ResultUI.Idle -> {}
                ResultUI.Loading -> {}
                is ResultUI.Success<*> -> {
                    val message = it.data as MessageResponse
                    val holder = (binding.viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(binding.viewPager.currentItem) as? VideoPagerAdapter.VideoViewHolder ?: return@observe
                    holder.binding.flFollowing.visibility = if (message.message == "Đã follow") View.INVISIBLE else View.VISIBLE
                    viewModel.updateStateFollowing()
                }
            }
        }*/


        //Refresh trang
        mainViewModel.refresh.observe(viewLifecycleOwner) {
            if (it) {
                refreshData()
                mainViewModel.setRefresh(false)
            }
        }
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
            if (it == Navigation.Profile || it == Navigation.User) {
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
