package com.github.libretube.ui.abbadi

import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.doOnLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Content now extends behind the translucent top bar and floating bottom nav.
 * Pads the first vertically-scrolling view (with clipToPadding=false so content
 * scrolls beneath the glass) or, if none touches the edge, the fragment root.
 */
object AbbadiGlassInsets {
    private const val TAG = 0x7A0B0B1

    fun register(fm: FragmentManager, top: () -> Int, bottom: () -> Int) {
        fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(f: FragmentManager, fr: Fragment, v: View, s: android.os.Bundle?) {
                v.doOnLayout { apply(v, top(), bottom()) }
            }
        }, false)
    }

    private fun isVertical(v: View): Boolean = when (v) {
        is RecyclerView -> (v.layoutManager as? LinearLayoutManager)?.orientation != LinearLayoutManager.HORIZONTAL
        is NestedScrollView, is ScrollView -> true
        else -> false
    }

    private fun findScrollable(root: View): View? {
        val q = ArrayDeque<View>(); q.add(root)
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            if (isVertical(v)) return v
            if (v is ViewGroup) for (i in 0 until v.childCount) q.add(v.getChildAt(i))
        }
        return null
    }

    private fun offsetIn(root: View, v: View): IntArray {
        val a = IntArray(2); val b = IntArray(2)
        root.getLocationInWindow(a); v.getLocationInWindow(b)
        return intArrayOf(b[1] - a[1], (a[1] + root.height) - (b[1] + v.height))
    }

    fun apply(root: View, top: Int, bottom: Int) {
        if (root.getTag(TAG) == true) return
        root.setTag(TAG, true)
        val sc = findScrollable(root)
        val off = sc?.let { offsetIn(root, it) }
        val topTarget = if (sc != null && off!![0] <= 1) sc else root
        val bottomTarget = if (sc != null && off!![1] <= 1) sc else root
        topTarget.setPadding(topTarget.paddingLeft, topTarget.paddingTop + top, topTarget.paddingRight, topTarget.paddingBottom)
        bottomTarget.setPadding(bottomTarget.paddingLeft, bottomTarget.paddingTop, bottomTarget.paddingRight, bottomTarget.paddingBottom + bottom)
        (sc as? ViewGroup)?.clipToPadding = false
    }
}
