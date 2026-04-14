package com.nlhd.appperformance.Feature.LoginScreen

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.databinding.BottomSheetLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginBottomSheet(

): BottomSheetDialogFragment() {

    private lateinit var _binding: BottomSheetLoginBinding
    private val binding get() = _binding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetLoginBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_sheet)

        viewModel.state.observe(viewLifecycleOwner) { resultUI ->
            when (resultUI) {
                ResultUI.Idle -> {}
                ResultUI.Loading -> {

                }
                is ResultUI.Success -> {

                }
                is ResultUI.Error<*> -> {
                    val message = resultUI.message
                    Log.d("AAA", "Login: $message")
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.edtEmail.text.toString(),
                binding.edtPassword.text.toString(),
                onDismissBottomSheet = {
                    dismiss()
                }
            )
        }

        val bottomSheetGeneral = view.parent as? View ?: return
        bottomSheetGeneral.let {
            val behavior = BottomSheetBehavior.from(it)

            val displayMetrics = Resources.getSystem().displayMetrics
            val height = (displayMetrics.heightPixels * 0.8).toInt()

            it.layoutParams.height = height
            behavior.peekHeight = height
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = MaterialShapeDrawable(
                ShapeAppearanceModel.builder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 8f)
                    .setTopRightCorner(CornerFamily.ROUNDED, 8f)
                    .build()
            )
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }
}