package com.nlhd.appperformance.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.nlhd.appperformance.Adapter.SearchItemAdapter
import com.nlhd.appperformance.databinding.FragmentSearchBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }*/

        val list = listOf(
            "hướng dẫn cách tắt tai nghe bluetooth",
            "#tiktok",
            "Apple",
            "Nghe gọi điện thoại",
            "Nhạc remix hay nhất"
        )
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SearchItemAdapter(list)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}