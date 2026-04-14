package com.nlhd.appperformance.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.nlhd.appperformance.Adapter.SearchSuccessAdapter
import com.nlhd.appperformance.DetailVideoActivity
import com.nlhd.appperformance.ViewModel.SearchSuccessViewModel
import com.nlhd.appperformance.databinding.FragmentSearchSuccessBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
@AndroidEntryPoint
class SearchSuccessFragment : Fragment() {

    private var _binding: FragmentSearchSuccessBinding? = null
    private lateinit var adapter: SearchSuccessAdapter
    private val viewModel: SearchSuccessViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: SearchSuccessFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchSuccessBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SearchSuccessAdapter(
            onClickCard = { position ->
                val intent = Intent(requireContext(), DetailVideoActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra("keyword", args.keyword)
                startActivity(intent)
            }
        )
        adapter.addLoadStateListener { loadStates ->
            val isLoading = loadStates.refresh is LoadState.Loading
            val isError = loadStates.refresh is LoadState.Error

            binding.loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvFeed.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.llError.visibility = if (isError) View.VISIBLE else View.GONE
        }
        binding.errorView.setOnClickListener {
            adapter.retry()
        }
        observeViewModel()
        binding.rvFeed.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@SearchSuccessFragment.adapter
            setHasFixedSize(true)
        }
    }

    @OptIn(UnstableApi::class)
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videos(args.keyword).collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}