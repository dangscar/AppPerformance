package com.nlhd.appperformance.Fragment

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.nlhd.appperformance.R
import com.nlhd.appperformance.ViewModel.ExploreViewModel
import com.nlhd.appperformance.databinding.FragmentExploreBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import com.nlhd.appperformance.Utils.TabSelected
import com.nlhd.appperformance.ViewModel.MainViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExploreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ExploreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private  var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SearchSuccessAdapter
    private val viewModel: ExploreViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var isLoaded = false
    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

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
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())

            insets?.let {
                binding.rvFeed.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top + 50.dpToPx()   // ✅ status bar + margin thêm
                }
            }
        }

        binding.rvFeed.clipToPadding = false
        adapter = SearchSuccessAdapter(
            onClickCard = { position ->
                val intent = Intent(requireContext(), DetailVideoActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra("type", "explore")
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }
        )

        adapter.addLoadStateListener { loadStates ->
            val isLoading = loadStates.refresh is LoadState.Loading
            val isError = loadStates.refresh is LoadState.Error

            binding.loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvFeed.visibility = if (isLoading || isError) View.GONE else View.VISIBLE
            binding.llError.visibility = if (isError) View.VISIBLE else View.GONE
        }

        binding.errorView.setOnClickListener {
            adapter.retry()
        }

        binding.rvFeed.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@ExploreFragment.adapter
            setHasFixedSize(true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isLoaded) {
            binding.flExplore.setBackgroundColor("#F2FFFFFF".toColorInt())
            binding.rvFeed.setBackgroundColor("#FFF2F2F2".toColorInt())
            observeViewModel()
            isLoaded = true
        }
        mainViewModel.setTabSelected(TabSelected.Explore)

    }

    @OptIn(UnstableApi::class)
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videos.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExploreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExploreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}