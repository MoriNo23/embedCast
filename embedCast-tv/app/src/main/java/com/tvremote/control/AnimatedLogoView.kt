package com.embedcast.tv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.animation.ValueAnimator
import android.animation.AnimatorSet
import android.view.animation.AccelerateDecelerateInterpolator

class AnimatedLogoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3d3d4a")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8a8a9a")
        textSize = 80f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5eead4")
        style = Paint.Style.FILL
    }

    private var dotAlpha = 255
    private var dotScale = 1f
    private var dotGlowRadius = 8f

    private val animatorSet = AnimatorSet()

    init {
        startPulseAnimation()
    }

    private fun startPulseAnimation() {
        // Alpha animation
        val alphaAnimator = ValueAnimator.ofInt(255, 128, 255).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                dotAlpha = it.animatedValue as Int
                invalidate()
            }
        }

        // Scale animation
        val scaleAnimator = ValueAnimator.ofFloat(1f, 0.7f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                dotScale = it.animatedValue as Float
                invalidate()
            }
        }

        // Glow animation
        val glowAnimator = ValueAnimator.ofFloat(8f, 4f, 8f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                dotGlowRadius = it.animatedValue as Float
                invalidate()
            }
        }

        animatorSet.playTogether(alphaAnimator, scaleAnimator, glowAnimator)
        animatorSet.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val size = minOf(width, height) * 0.8f
        val radius = size / 2f

        // Draw background rounded rect
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        canvas.drawRoundRect(rect, 16f, 16f, bgPaint)

        // Draw "E" text
        val textY = centerY + textPaint.textSize / 3
        canvas.drawText("E", centerX, textY, textPaint)

        // Calculate dot position (top-right corner of the rounded rect)
        val dotBaseX = centerX + radius * 0.6f
        val dotBaseY = centerY - radius * 0.6f
        val dotBaseRadius = 12f

        // Draw glow effect
        dotPaint.alpha = (dotAlpha * 0.3f).toInt()
        canvas.drawCircle(
            dotBaseX,
            dotBaseY,
            dotBaseRadius * dotScale + dotGlowRadius,
            dotPaint
        )

        // Draw main dot
        dotPaint.alpha = dotAlpha
        canvas.drawCircle(
            dotBaseX,
            dotBaseY,
            dotBaseRadius * dotScale,
            dotPaint
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animatorSet.cancel()
    }
}
