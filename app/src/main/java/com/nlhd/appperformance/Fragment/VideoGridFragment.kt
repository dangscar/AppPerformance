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
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.Adapter.VideoStorePagingAdapter
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Activity.VideoStoreActivity
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.ViewModel.VideoStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class VideoGridFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: VideoStoreViewModel by viewModels()
    private lateinit var adapter: VideoStorePagingAdapter
    private lateinit var rv: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_grid, container, false)
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = VideoStorePagingAdapter(
            onClickCard = { position ->
                val intent = Intent(requireContext(), VideoStoreActivity::class.java)
                intent.putExtra("position", position)
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    R.anim.zoom_in, R.anim.zoom_out
                )
            }
        )
        rv.layoutManager = GridLayoutManager(requireContext(), 3)
        rv.adapter = adapter


        lifecycleScope.launch {
            viewModel.videoStore.collectLatest {
                adapter.submitData(it)
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
        rv.scrollToPosition(0)
    }

    companion object {
        fun newInstance(tab: Int) = VideoGridFragment().apply {
            arguments = bundleOf("TAB" to tab)
        }
    }
}