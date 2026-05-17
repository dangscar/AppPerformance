package com.nlhd.appperformance.BottomSheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nlhd.appperformance.Adapter.IconTextLayout.UserShareAdapter
import com.nlhd.appperformance.Adapter.IconTextLayout.UserShareItem
import com.nlhd.appperformance.R
import com.nlhd.appperformance.databinding.BottomSheetShareBinding

class BottomSheetShare : BottomSheetDialogFragment() {
    private var _binding: BottomSheetShareBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetShareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserList()
        setupActions()
    }

    private fun setupUserList() {
        val users = listOf(
            UserShareItem("Đăng lại", R.drawable.repost_new),
            UserShareItem("Minh Huy", R.drawable.asus),
            UserShareItem("Soo", R.drawable.asus),
            UserShareItem("Văn Khiêm", R.drawable.asus),
            UserShareItem("ngạn nè", R.drawable.asus),
            UserShareItem("Tô Quốc Khánh CTU", R.drawable.asus)
        )
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = UserShareAdapter(users)
        }
    }

    private fun setupActions() {
        // Cấu hình các mục hành động chính
        binding.itemDownload.apply {
            imgIcon.setImageResource(R.drawable.download)
            tvText.text = "Tải về"
        }
        
        binding.itemNotInterested.apply {
            imgIcon.setImageResource(R.drawable.ic_favorite_border)
            tvText.text = "Không quan tâm"
        }
        
        binding.itemReport.apply {
            imgIcon.setImageResource(R.drawable.report)
            tvText.text = "Báo cáo"
        }

        // Cấu hình phần cài đặt phát
        binding.itemSimpleMode.apply {
            imgIcon.setImageResource(R.drawable.pip)
            tvText.text = "Màn hình giản lược"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
