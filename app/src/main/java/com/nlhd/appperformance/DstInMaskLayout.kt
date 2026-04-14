package com.nlhd.appperformance

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout

class DstInMaskLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }



    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.saveLayer(
            0f, 0f,
            width.toFloat(),
            height.toFloat(),
            null
        )

        // === drawContent() ===
        super.dispatchDraw(canvas)


        val fadePercent = 0.07f // giống Compose của bạn
        val w = width.toFloat()

        val shader = LinearGradient(
            0f, 0f, w, 0f,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(128, 0, 0, 0), // Black 0.5
                Color.BLACK,
                Color.BLACK,
                Color.argb(128, 0, 0, 0),
                Color.TRANSPARENT
            ),
            floatArrayOf(
                0f,
                fadePercent * 0.55f,
                fadePercent,
                1f - fadePercent,
                1f - fadePercent*0.55f,
                1f
            ),
            Shader.TileMode.CLAMP
        )

        maskPaint.shader = shader



        // === drawRect(brush, DstIn) ===
        canvas.drawRect(
            0f, 0f,
            width.toFloat(),
            height.toFloat(),
            maskPaint
        )


        canvas.restoreToCount(save)
    }
}
