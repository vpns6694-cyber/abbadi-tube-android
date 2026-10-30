package com.github.libretube.ui.abbadi

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin

/**
 * ABBADI Tube background: deep burgundy with slowly drifting, soft red/pink light blobs.
 * Original drawing code; radial gradients fake the blur cheaply on every API level.
 */
class GlowBlobsDrawable : Drawable(), Animatable {
    private val bg = 0xFF070708.toInt()
    private val blobs = listOf(
        Blob(0x1AFFFFFF.toInt(), 0.15f, 0.12f, 0.75f, 0.0f),
        Blob(0x14808090.toInt(), 0.85f, 0.40f, 0.65f, 2.1f),
        Blob(0x10FFFFFF.toInt(), 0.35f, 0.80f, 0.55f, 4.2f),
        Blob(0x123A0A14.toInt(), 0.75f, 0.95f, 0.70f, 1.0f)
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f
    private val animator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat()).apply {
        duration = 40_000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { phase = it.animatedValue as Float; invalidateSelf() }
    }

    private data class Blob(val color: Int, val x: Float, val y: Float, val r: Float, val offset: Float)

    override fun draw(canvas: Canvas) {
        val b = bounds
        canvas.drawColor(bg)
        val w = b.width().toFloat()
        val h = b.height().toFloat()
        val base = maxOf(w, h)
        for (blob in blobs) {
            val cx = b.left + w * blob.x + cos(phase + blob.offset) * w * 0.08f
            val cy = b.top + h * blob.y + sin(phase * 0.8f + blob.offset) * h * 0.05f
            val radius = base * blob.r * 0.6f
            paint.shader = RadialGradient(cx, cy, radius, blob.color, blob.color and 0x00FFFFFF, Shader.TileMode.CLAMP)
            canvas.drawCircle(cx, cy, radius, paint)
        }
    }

    override fun start() { if (!animator.isStarted) animator.start() }
    override fun stop() { animator.cancel() }
    override fun isRunning() = animator.isRunning
    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.OPAQUE
}
