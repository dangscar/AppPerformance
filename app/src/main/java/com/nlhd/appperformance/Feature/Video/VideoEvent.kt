package com.nlhd.appperformance.Feature.Video

import android.view.View
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.nlhd.appperformance.Adapter.VideoPagerAdapter
import com.nlhd.appperformance.R

@OptIn(UnstableApi::class)
fun onChangeBottomSheet(
    width: Int,
    height: Int,
    offsetY: Float,
    holder: VideoPagerAdapter.VideoViewHolder,
    statusBarHeight: Int,
    onDoNotShow: () -> Unit,
    onShowSearchIcon: () -> Unit
) {
    val offset = offsetY   // [-1 .. 0]
    val aspectRatio = holder.binding.playerView.width.toFloat() / holder.binding.playerView.height.toFloat()
    val minScale = 0.16f + (aspectRatio * 0.68f)
    val progresses = (1f + offset).coerceIn(0f, 1f)
    val scale = 1f - (1f - minScale) * progresses
    val delta = height - height * scale

    var translateY = -delta / 2f + statusBarHeight * progresses * 0.25f
    var pivotY = width/4f
    if (aspectRatio > 15/9f) {
        translateY = -translateY
        pivotY = width/1f
    } else if (aspectRatio >= 1f) {
        pivotY = width/1f
        translateY += -delta * 2
    } else if (aspectRatio > 9/16f) {
        pivotY = width/1f
        translateY += -delta * 1.5f * aspectRatio
    }
    holder.binding.playerView.apply {
        this.pivotY = width/2f
        scaleX = scale
        scaleY = scale
        translationY = -delta / 2f + statusBarHeight * progresses * 0.25f
    }

    if (offset != -1f) {
        onDoNotShow()
        /*mainViewModel.showBarAction(false)
        layoutAlpha(holder, 0f)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)*/
        /*requireActivity().enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
        )*/
    }

    val progress = (1f + offsetY).coerceIn(0f, 1f)

    if (progress > 0f) {
        onShowSearchIcon()
    }

}