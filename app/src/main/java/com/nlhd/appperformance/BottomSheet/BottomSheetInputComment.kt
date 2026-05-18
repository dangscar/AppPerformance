package com.nlhd.appperformance.BottomSheet

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.nlhd.appperformance.Adapter.EmojiAdapter
import com.nlhd.appperformance.Feature.LoginScreen.LoginBottomSheet
import com.nlhd.appperformance.R
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.ViewModel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class BottomSheetInputComment(
    private val imageUrl: String = "",
    private val text: String,
    private val onChangeText: (String) -> Unit,
    private val onDone: () -> Unit
): BottomSheetDialogFragment() {
    private lateinit var edtInputComment: EditText
    private lateinit var iv_avatar: ImageView
    private lateinit var rv_emoji: RecyclerView
    private lateinit var rv_icon: RecyclerView
    private lateinit var ll_actionInput: LinearLayout
    private val viewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_input_comment, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(dialog.window!!, false)
        dialog.window?.setWindowAnimations(0)
        dialog.setOnShowListener {
            val decorView = dialog.window?.decorView ?: return@setOnShowListener
            WindowCompat.setDecorFitsSystemWindows(dialog.window!!, false)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // Android 11+ dùng WindowInsets
                ViewCompat.setOnApplyWindowInsetsListener(decorView) { _, insets ->
                    val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                    val keyboardHeight = ime - nav

                    if (keyboardHeight > 0 && keyboardHeight != viewModel.text.value) {
                        viewModel.setText(keyboardHeight)
                    }
                    insets
                }
            } else {
                // Android 10 trở xuống dùng GlobalLayoutListener
                decorView.viewTreeObserver.addOnGlobalLayoutListener {
                    val rect = Rect()
                    decorView.getWindowVisibleDisplayFrame(rect)
                    val keyboardHeight = decorView.height - rect.bottom

                    if (keyboardHeight > 200 && keyboardHeight != viewModel.text.value) {
                        viewModel.setText(keyboardHeight)
                    }
                }
            }

            val bottomSheet =
                dialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                )

            bottomSheet?.background = MaterialShapeDrawable(
                ShapeAppearanceModel.builder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 8f)
                    .setTopRightCorner(CornerFamily.ROUNDED, 8f)
                    .build()
            )
            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.apply {
            window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
            )
        }
        return dialog
        /*return BottomSheetDialog(requireContext(), theme).apply {
            window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }*/
    }



    private fun getKeyboardHeight(): Int {
        val rect = Rect()
        requireActivity().window.decorView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = requireActivity().window.decorView.height
        return screenHeight - rect.bottom
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_sheet)
        edtInputComment = view.findViewById<EditText>(R.id.edtInputComment)
        iv_avatar = view.findViewById<ImageView>(R.id.iv_avatar)
        rv_emoji = view.findViewById<RecyclerView>(R.id.rv_emoji)
        rv_icon = view.findViewById<RecyclerView>(R.id.rv_icon)
        ll_actionInput = view.findViewById<LinearLayout>(R.id.ll_actionInput)

        viewModel.text.observe(viewLifecycleOwner) { text->
            if (text > 0) {
                rv_icon.updateLayoutParams {
                    height = text
                }
            }
        }

        Glide.with(iv_avatar).load(imageUrl).error(R.drawable.asus).into(iv_avatar)
        edtInputComment.post {
            edtInputComment.requestFocus()
            val imm = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager

            imm.showSoftInput(edtInputComment, InputMethodManager.SHOW_IMPLICIT)

        }
        edtInputComment.setText(text)
        edtInputComment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onDone()
                dismiss()
                true
            } else {
                false
            }
        }
        edtInputComment.doOnTextChanged { text, _, _, _ ->
            onChangeText(edtInputComment.text.toString())
        }

        val emojiList = listOf(
            R.drawable.u1f602_u1f602,
            R.drawable.u1f604_u1f601,
            R.drawable.u1f970_u2764_ufe0f,
            R.drawable.u1f603_u1f633,
            R.drawable.u1f603_u1f612,
            R.drawable.u1f604_u1f605,
            R.drawable.u1f641_u1f622
        )

        val adapter = EmojiAdapter(emojiList) { emoji ->
            // xử lý khi click
        }

        rv_emoji.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        rv_emoji.adapter = adapter


    }


}