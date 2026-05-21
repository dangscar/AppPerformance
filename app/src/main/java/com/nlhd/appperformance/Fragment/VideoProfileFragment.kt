package com.nlhd.appperformance.Fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.Adapter.VideoStorePagingAdapter
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Activity.VideoStoreActivity
import com.nlhd.appperformance.Adapter.VideoProfileAdapter
import com.nlhd.appperformance.DetailVideoActivity
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.ViewModel.VideoProfileViewModel
import com.nlhd.appperformance.ViewModel.VideoStoreViewModel
import com.nlhd.appperformance.databinding.FragmentVideoGridBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class VideoProfileFragment : Fragment() {

    private var _binding: FragmentVideoGridBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: VideoProfileViewModel by viewModels()

    private lateinit var adapter: VideoProfileAdapter
    var idProfile = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoGridBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VideoProfileAdapter(
            onClickCard = { position ->
                val intent = Intent(requireContext(), DetailVideoActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra("type", "profile")
                intent.putExtra("userId", mainViewModel.idProfile.value.toString())
                intent.putExtra("timestamp", viewModel.timestamp.value)
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    R.anim.zoom_in, R.anim.zoom_out
                )
            }
        )

        adapter.addLoadStateListener { loadStates ->
            val isLoading = loadStates.refresh is LoadState.Loading
            val isError = loadStates.refresh is LoadState.Error

            binding.loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.llError.visibility = if (isError) View.VISIBLE else View.GONE
            if (loadStates.refresh is LoadState.NotLoading) {
                binding.recyclerView.visibility = View.VISIBLE
                binding.loadingView.visibility = View.GONE
                binding.llError.visibility = View.GONE
            }

        }
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.adapter = adapter

        binding.errorView.setOnClickListener {
            adapter.retry()
            Log.d("AAA", "Retry")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                combine(
                    mainViewModel.idProfile.asFlow(),
                    mainViewModel.navigation.asFlow()
                ) { id, navigation ->
                    Pair(id, navigation)
                }
                    .filter { (_, navigation) ->
                        navigation == Navigation.User
                    }
                    .distinctUntilChanged()
                    .onEach { (id, _) ->

                        if (id != idProfile) {
                            viewModel.clearProfileFlow(
                                idProfile.toString(),
                                viewModel.timestamp.value!!
                            )

                            idProfile = id
                        }
                    }
                    .flatMapLatest { (id, _) ->

                        viewModel.videoProfile(
                            id.toString(),
                            viewModel.timestamp.value!!
                        )
                    }
                    .collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
            }
        }


        //Thiết lập scrollToTop cho recyclerView của video
        mainViewModel.scrollToTopRecyclerView.observe(viewLifecycleOwner) {
            if (it) {
                scrollToTop()
                mainViewModel.setScrollToTopRecyclerView(false)
            }
        }

    }

    fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
    }

    companion object {
        fun newInstance(tab: Int) = VideoProfileFragment().apply {
            arguments = bundleOf("TAB" to tab)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}