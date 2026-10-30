package com.github.libretube.ui.abbadi

import android.animation.ValueAnimator
import android.content.Context
import android.provider.Settings
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.github.libretube.R

/**
 * ABBADI Tube official wordmark. Static logo; only the red play badge on "Tube"
 * glows and breathes. Respects the system "remove animations" setting.
 * Abdalla Abbadi · connect@3bbadi.com
 */
class AbbadiLogoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val base = ImageView(context).apply {
        setImageResource(R.drawable.abbadi_logo); scaleType = ImageView.ScaleType.FIT_XY
        contentDescription = context.getString(R.string.app_name)
    }
    private val glow = ImageView(context).apply { setImageResource(R.drawable.abbadi_logo_glow); scaleType = ImageView.ScaleType.FIT_XY; alpha = 0.5f }
    private val badge = ImageView(context).apply { setImageResource(R.drawable.abbadi_logo_badge); scaleType = ImageView.ScaleType.FIT_XY }
    private var animator: ValueAnimator? = null
    private var sweepPos = -1f
    private val sweepPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    private val clip = android.graphics.Path()

    init {
        clipChildren = false; clipToPadding = false
        addView(glow); addView(base); addView(badge)
        setWillNotDraw(false)
    }

    // light sweep across the play badge only
    override fun dispatchDraw(canvas: android.graphics.Canvas) {
        super.dispatchDraw(canvas)
        if (sweepPos < 0f || badge.width == 0) return
        val l = badge.left.toFloat(); val t = badge.top.toFloat(); val bw = badge.width.toFloat(); val bh = badge.height.toFloat()
        clip.reset(); clip.addRoundRect(l + bw * 0.08f, t + bh * 0.1f, l + bw * 0.92f, t + bh * 0.9f, bh * 0.22f, bh * 0.22f, android.graphics.Path.Direction.CW)
        val x = l - bw * 0.5f + sweepPos * bw * 2f
        sweepPaint.shader = android.graphics.LinearGradient(x - bw * 0.25f, t, x + bw * 0.25f, t + bh,
            intArrayOf(0x00FFFFFF, 0x99FFFFFF.toInt(), 0x00FFFFFF), null, android.graphics.Shader.TileMode.CLAMP)
        canvas.save(); canvas.clipPath(clip); canvas.drawRect(l, t, l + bw, t + bh, sweepPaint); canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val w = (h * RATIO).toInt()
        super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val w = (r - l).toFloat(); val h = (b - t).toFloat()
        base.layout(0, 0, w.toInt(), h.toInt())
        fun place(v: ImageView, f: FloatArray) {
            v.layout((f[0] * w).toInt(), (f[1] * h).toInt(), (f[2] * w).toInt(), (f[3] * h).toInt())
            v.pivotX = (v.width / 2f); v.pivotY = (v.height / 2f)
        }
        place(glow, GLOW); place(badge, BADGE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        if (scale == 0f) { glow.alpha = 0.6f; return }
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2400; repeatMode = ValueAnimator.RESTART; repeatCount = ValueAnimator.INFINITE
            interpolator = android.view.animation.LinearInterpolator()
            addUpdateListener {
                val f = it.animatedValue as Float
                // pulse: 0..1..0 over first 60%, then light sweep across the badge
                val p = if (f < 0.6f) Math.sin(Math.PI * f / 0.6f).toFloat() else 0f
                glow.alpha = 0.35f + 0.65f * p
                glow.scaleX = 1.0f + 0.45f * p; glow.scaleY = glow.scaleX
                badge.scaleX = 1f + 0.12f * p; badge.scaleY = badge.scaleX
                sweepPos = if (f >= 0.55f) (f - 0.55f) / 0.45f else -1f
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() { animator?.cancel(); animator = null; super.onDetachedFromWindow() }

    companion object {
        private const val RATIO = 3304f / 755f
        private val BADGE = floatArrayOf(2533f / 3304, 264f / 755, 2743f / 3304, 421f / 755)
        private val GLOW = floatArrayOf(2455f / 3304, 186f / 755, 2821f / 3304, 499f / 755)
    }
}
