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

    init {
        clipChildren = false; clipToPadding = false
        addView(glow); addView(base); addView(badge)
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
            duration = 1600; repeatMode = ValueAnimator.REVERSE; repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val t = it.animatedValue as Float
                glow.alpha = 0.3f + 0.7f * t
                glow.scaleX = 0.95f + 0.2f * t; glow.scaleY = glow.scaleX
                badge.scaleX = 1f + 0.06f * t; badge.scaleY = badge.scaleX
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
