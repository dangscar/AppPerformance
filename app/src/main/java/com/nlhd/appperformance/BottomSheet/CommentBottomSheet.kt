package com.nlhd.appperformance.BottomSheet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.nlhd.appperformance.Activity.SearchActivity
import com.nlhd.appperformance.Adapter.CommentPagerAdapter
import com.nlhd.appperformance.Feature.LoginScreen.LoginBottomSheet
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.ViewModel.CommentViewModel
import com.nlhd.appperformance.ViewModel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.toString

data class Component(
    val commentCount: Int
)
@AndroidEntryPoint
class CommentBottomSheet(
    private val viewModel: CommentViewModel,
    private val videoId: String = "",
    val onChangeBottomSheet: (Int, Int, Float) -> Unit,
    val onDismiss: () -> Unit,
    val onShow: () -> Unit,
    private val onChangeComponent: (Component)-> Unit
): BottomSheetDialogFragment() {

    private lateinit var ivClose: ImageView
    private lateinit var llBottomInput: LinearLayout
    private var isOnSlide: Boolean = false
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var loadingView: LottieAnimationView

    private lateinit var commentPagerAdapter: CommentPagerAdapter
    private lateinit var edtComment: EditText
    private lateinit var iv_avatar: ImageView
    private lateinit var ll_comment: LinearLayout
    private var imageUrl = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_comment, container, false)
    }
    // Extension
    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            dialog?.window?.apply {
                setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                )
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

                val decorView = decorView as? ViewGroup ?: return@apply
                if (decorView.findViewWithTag<View>("search_icon") != null) return@apply

                val ivSearch = ImageView(requireContext()).apply {
                    tag = "search_icon"
                    setImageResource(R.drawable.ic_search)
                    imageTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                    layoutParams = FrameLayout.LayoutParams(
                        26.dpToPx(), 26.dpToPx()
                    ).apply {
                        gravity = Gravity.TOP or Gravity.END
                        topMargin = 12.dpToPx()
                        marginEnd = 14.dpToPx()
                    }
                    setOnClickListener {
                        val intent = Intent(requireActivity(),SearchActivity::class.java)
                        startActivity(intent)
                    }
                }
                decorView.addView(ivSearch)
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

            val sheet = dialog!!
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            behavior = BottomSheetBehavior.from(sheet)

            behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.addBottomSheetCallback(bottomSheetCallback)

            // Lấy view scrim (vùng tối bên ngoài)
            val scrim = dialog.window?.decorView?.findViewById<View>(
                com.google.android.material.R.id.touch_outside
            )
            scrim?.setOnClickListener {
                // Click bên ngoài bottomSheet
                onShow()
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                dismissNow()
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_sheet)

        val bottomSheet = view.findViewById<LinearLayout>(R.id.bottomSheetComment)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvComments)
        val bottomBar = view.findViewById<CardView>(R.id.bottomBarComment)
        ivClose = bottomSheet.findViewById<ImageView>(R.id.ivClose)
        llBottomInput = view.findViewById(R.id.ll_bottomInput)
        loadingView = view.findViewById(R.id.loadingView)
        edtComment = view.findViewById(R.id.edtComment)
        val tv_commentCount = view.findViewById<TextView>(R.id.tv_commentCount)
        iv_avatar = view.findViewById(R.id.iv_avatar)
        ll_comment = view.findViewById<LinearLayout>(R.id.ll_comment)
        val btn_comment = view.findViewById<Button>(R.id.btn_comment)


        val bottomSheetGeneral = view.parent as? View ?: return
        bottomSheetGeneral.let {
            val behavior = BottomSheetBehavior.from(it)

            val displayMetrics = Resources.getSystem().displayMetrics
            val height = (displayMetrics.heightPixels * 0.68).toInt()

            it.layoutParams.height = height
            behavior.peekHeight = height
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isHideable = true
            behavior.significantVelocityThreshold = 50
            behavior.hideFriction = -1f
        }
        bottomSheetGeneral.doOnNextLayout {
            val width = bottomSheet.measuredWidth
            val height = bottomSheet.measuredHeight

            bottomSheetAnimator = ValueAnimator.ofFloat(-1f, 0f).apply {
                duration = 150L
                interpolator = FastOutSlowInInterpolator()
                addUpdateListener {
                    onChangeBottomSheet(width, height, it.animatedValue as Float)
                }
                start()
            }
        }


        bottomBar.setOnClickListener {
            bottomSheetInputComment()
        }

        edtComment.setOnClickListener {
            bottomSheetInputComment()
        }

        ivClose.setOnClickListener {
            onShow()
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            dismissNow()
        }

        lifecycleScope.launch {
            viewModel.userState.collect { userPreference ->
                if (!userPreference.isLoggedIn) {

                } else {
                    if (userPreference.token.isNotEmpty()){
                        Glide.with(iv_avatar).load(userPreference.avatarUrl).error(R.drawable.asus).into(iv_avatar)
                        imageUrl = userPreference.avatarUrl
                    }
                }
            }
        }

        commentPagerAdapter = CommentPagerAdapter()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = commentPagerAdapter
        }

        observeData()
        commentPagerAdapter.addLoadStateListener { loadStates ->
            val isLoading = loadStates.refresh is LoadState.Loading
            val isNotLoading = loadStates.refresh is LoadState.NotLoading
            loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
            tv_commentCount.text = if (isNotLoading) "${commentPagerAdapter.itemCount} bình luận" else "Bình luận"
            if (isNotLoading) {
                onChangeComponent(Component(
                    commentCount = commentPagerAdapter.itemCount
                ))
            }

            //Handle 0 comment
            if (isLoading) {
                ll_comment.visibility = View.GONE
            }
            if (isNotLoading && commentPagerAdapter.itemCount == 0){
                tv_commentCount.text = "Bình luận"
                ll_comment.visibility = View.VISIBLE
            }
        }

        btn_comment.setOnClickListener {
            bottomSheetInputComment()
        }


        llBottomInput.post {
            val inputHeight = llBottomInput.height
            recyclerView.setPadding(
                recyclerView.paddingLeft,
                recyclerView.paddingTop,
                recyclerView.paddingRight,
                inputHeight
            )
        }


        //AddCommentState
        viewModel.addCommentState.observe(viewLifecycleOwner) {
            when (it) {
                is ResultUI.Error<*> -> {
                }
                ResultUI.Idle -> {
                }
                ResultUI.Loading -> {
                }
                is ResultUI.Success<*> -> {
                    edtComment.setText("")
                    commentPagerAdapter.refresh()
                    viewModel.setAddCommentState(ResultUI.Idle)
                }
            }
        }



    }

    fun bottomSheetInputComment() {
        val bottomSheetInput = BottomSheetInputComment(
            imageUrl = imageUrl,
            text = edtComment.text.toString(),
            onChangeText = {
                edtComment.setText(it)
            },
            onDone = {
                viewModel.addComment(videoId, edtComment.text.toString())
            }
        )
        bottomSheetInput.show(
            requireActivity().supportFragmentManager,
            BottomSheetInputComment::class.java.simpleName
        )
    }

    private fun observeData() {
        viewModel.loadComment(videoId)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.commentFlow.collectLatest {
                    commentPagerAdapter.submitData(it)
                }
            }
        }
    }

    private var lastSlideOffset = 0f
    private var previousSlideOffset = 0f
    private var isDraggingDown = false


    val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (!isOnSlide) isOnSlide = true

            //Bắt sự kiện nếu là như kéo xuống thật thì hiển thị actionBar
            isDraggingDown = slideOffset < previousSlideOffset
            previousSlideOffset = slideOffset


            lastSlideOffset = slideOffset
            val width = bottomSheet.width
            val height = bottomSheet.height
            onChangeBottomSheet(width, height, slideOffset)
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    if (isOnSlide) isOnSlide = false
                    Log.d("AAA", "STATE_EXPANDED")
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    if (isOnSlide) isOnSlide = false
                    Log.d("AAA", "STATE_COLLAPSED")
                }
                BottomSheetBehavior.STATE_SETTLING -> {
                    Log.d("AAA", "STATE_SETTLING")
                    if (isDraggingDown) {
                        onShow()
                    }
                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                    Log.d("AAA", "STATE_DRAGGING")
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    Log.d("AAA", "STATE_HIDDEN")
                }
                BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    Log.d("AAA", "STATE_HALF_EXPANDED")
                }

            }
        }
    }

    private var bottomSheetAnimator: ValueAnimator? = null

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val decorView = (dialog as? Dialog)?.window?.decorView as? ViewGroup
        decorView?.findViewWithTag<View>("search_icon")?.let {
            decorView.removeView(it)
        }

        val bottomSheet = view?.parent as? View
        if (bottomSheet != null) {
            val width = bottomSheet.width
            val height = bottomSheet.height
            dismissOnSlide(width, height)
        }

    }

    fun dismissOnSlide(width: Int, height: Int) {
        if (!isOnSlide) {
            bottomSheetAnimator = ValueAnimator.ofFloat(0f, -1f).apply {
                duration = 100L
                interpolator = FastOutSlowInInterpolator()

                addUpdateListener {
                    onChangeBottomSheet(width, height, it.animatedValue as Float)
                }

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        releaseAnimator()
                        onDismiss()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        super.onAnimationCancel(animation)
                        releaseAnimator()
                    }
                })
                start()
            }
        } else {
            // Không animate → coi như đã "end"
            onChangeBottomSheet(width, height, -1f)
            onDismiss()
        }

        isOnSlide = false
    }

    private fun releaseAnimator() {
        bottomSheetAnimator?.apply {
            removeAllUpdateListeners()
            removeAllListeners()
            cancel()
        }
        bottomSheetAnimator = null
    }



    override fun getTheme(): Int {
        return R.style.BottomSheetNoScrim
    }
}