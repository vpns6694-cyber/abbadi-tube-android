package com.github.libretube.ui.abbadi

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.animation.LinearInterpolator
import com.google.android.material.bottomnavigation.BottomNavigationView

/** Icon drawn with a flowing red <-> burgundy gradient (selected nav item only). */
class FlowingGradientIcon(private val base: Drawable, animate: Boolean) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
    private val m = Matrix()
    private var phase = 0f
    private var shaderW = -1
    val animator: ValueAnimator? = if (!animate) null else ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2200; repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator()
        addUpdateListener { phase = it.animatedValue as Float; invalidateSelf() }
        start()
    }

    override fun draw(canvas: Canvas) {
        val b = bounds; if (b.isEmpty) return
        val w = b.width()
        if (shaderW != w) {
            shaderW = w
            paint.shader = LinearGradient(0f, 0f, w.toFloat(), w.toFloat(),
                intArrayOf(0xFFFF4D63.toInt(), 0xFFE0263F.toInt(), 0xFF8A0F24.toInt(), 0xFFE0263F.toInt(), 0xFFFF4D63.toInt()),
                null, Shader.TileMode.REPEAT)
        }
        m.setTranslate(b.left + phase * w * 2f, b.top + phase * w * 2f)
        paint.shader.setLocalMatrix(m)
        val sc = canvas.saveLayer(b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.bottom.toFloat(), null)
        base.bounds = b; base.setTint(0xFFFFFFFF.toInt()); base.draw(canvas)
        canvas.drawRect(b, paint)
        canvas.restoreToCount(sc)
    }
    override fun getIntrinsicWidth() = base.intrinsicWidth
    override fun getIntrinsicHeight() = base.intrinsicHeight
    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}

object AbbadiNavStyler {
    private val originals = HashMap<Int, Drawable.ConstantState>()
    private var current: FlowingGradientIcon? = null

    fun refresh(nav: BottomNavigationView, selectedId: Int = nav.selectedItemId) {
        val animate = Settings.Global.getFloat(nav.context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        nav.itemIconTintList = null
        current?.animator?.cancel(); current = null
        for (i in 0 until nav.menu.size()) {
            val item = nav.menu.getItem(i)
            val st = originals.getOrPut(item.itemId) { item.icon?.constantState ?: continue }
            val d = st.newDrawable(nav.resources).mutate()
            if (item.itemId == selectedId) {
                current = FlowingGradientIcon(d, animate); item.icon = current
            } else {
                d.setTint(0xB3FFFFFF.toInt()); item.icon = d
            }
        }
    }
}
