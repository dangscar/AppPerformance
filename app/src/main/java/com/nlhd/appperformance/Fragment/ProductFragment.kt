package com.nlhd.appperformance.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import com.nlhd.appperformance.Adapter.Shop.Product
import com.nlhd.appperformance.Adapter.Shop.ProductPagingAdapter
import com.nlhd.appperformance.R
import com.nlhd.appperformance.databinding.FragmentProductBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = listOf(
            Product("Mỹ phẩm 1", "https://down-vn.img.susercontent.com/file/d4e14f20fbcb6e42c2adc631536ca1c9"),
            Product("Mỹ phẩm 2", "https://down-vn.img.susercontent.com/file/2d5ab3f5a7d60b6f5f7818921b53e524"),
            Product("Mỹ phẩm 3", "https://down-vn.img.susercontent.com/file/ce3aedb12a4bdd13b7ebb1988b171eef"),
            Product("Mỹ phẩm 4", "https://down-vn.img.susercontent.com/file/70c0e8b31dccb810d4caa1e188d4cd8f"),
            Product("Mỹ phẩm 5", "https://down-vn.img.susercontent.com/file/84de0199314a47ba993cbae203393ce1"),
            Product("Mỹ phẩm 6", "https://down-vn.img.susercontent.com/file/9a67b555ee38eeacc8f483cad33d535c"),
            Product("Mỹ phẩm 7", "https://down-vn.img.susercontent.com/file/c8169887b60e20a161d0288e6f9a193e"),
            Product("Màn hình OC", "https://down-vn.img.susercontent.com/file/61fafd8b169dc1a1f41324f019eb8e8a"),
        )

        binding.rvProduct.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
            adapter = ProductPagingAdapter(list)
        }
    }
}