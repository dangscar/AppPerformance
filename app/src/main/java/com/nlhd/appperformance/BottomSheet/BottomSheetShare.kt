package com.nlhd.appperformance.BottomSheet

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.nlhd.appperformance.Adapter.IconTextLayout.AttributeSheetAdapter
import com.nlhd.appperformance.Adapter.IconTextLayout.AttributeSheetItem
import com.nlhd.appperformance.R
import com.nlhd.appperformance.databinding.BottomSheetShareBinding

class BottomSheetShare: BottomSheetDialogFragment() {
    private var _binding : BottomSheetShareBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetShareBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                )

            bottomSheet?.background = MaterialShapeDrawable(
                ShapeAppearanceModel.builder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 12f)
                    .setTopRightCorner(CornerFamily.ROUNDED, 12f)
                    .build()
            )

            val sheet = dialog!!
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            var behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetGeneral = view.parent as? View ?: return
        bottomSheetGeneral.let {
            val behavior = BottomSheetBehavior.from(it)

            val displayMetrics = Resources.getSystem().displayMetrics
            val height = (displayMetrics.heightPixels * 0.6).toInt()

            it.layoutParams.height = height
            behavior.peekHeight = height
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isHideable = true
        }

        binding.rvAction.apply {
            val list = listOf(
                AttributeSheetItem(icon = R.drawable.autoscroll, text = "AutoScroll"),
                AttributeSheetItem(icon = R.drawable.report, text = "Report"),
                AttributeSheetItem(icon = R.drawable.pip, text = "Pip"),
                AttributeSheetItem(icon = R.drawable.send, text = "Send"),
                AttributeSheetItem(icon = R.drawable.download, text = "Download"),
            )
            layoutManager = LinearLayoutManager(context)
            adapter = AttributeSheetAdapter(list)
        }

    }

}