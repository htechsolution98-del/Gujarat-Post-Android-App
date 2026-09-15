package com.gujaratpost.app.ui.detail

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * NewspaperPageTransformer creates an authentic, physical 3D newspaper page turn animation.
 *
 * When swiping left (to read next story):
 * - The left spine stays anchored at x=0 (cancelling ViewPager2 default slide).
 * - The current page turns/flips over to the left along the 3D Y-axis.
 * - The next page unfolds smoothly into view from the right.
 *
 * When swiping right (to read previous story):
 * - The right spine stays anchored at x=width (cancelling ViewPager2 default slide).
 * - The current page turns/flips over to the right along the 3D Y-axis.
 * - The previous page unfolds smoothly into view from the left.
 */
class NewspaperPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        try {
            val width = view.width.toFloat()
            val height = view.height.toFloat()
            if (width <= 0f || height <= 0f) return

            // 3D perspective camera distance (prevents near-plane clipping)
            view.cameraDistance = 25000f * view.resources.displayMetrics.density

        when {
            position < -1f -> {
                // Completely off-screen to the left
                view.alpha = 0f
                view.visibility = View.INVISIBLE
                view.translationX = 0f
                view.rotationY = 0f
            }
            position <= 0f -> {
                // Left page: As position goes 0 -> -1, turns to the left around left spine
                view.visibility = View.VISIBLE

                // CRITICAL: Cancels ViewPager2 horizontal translation so the spine stays pinned
                view.translationX = -position * width

                // Pivot on left edge
                view.pivotX = 0f
                view.pivotY = height * 0.5f

                // Rotate around Y-axis (0 deg to -90 deg)
                view.rotationY = 90f * position

                val absPos = abs(position)
                // Elevation hierarchy
                view.elevation = (1f - absPos) * 30f + 10f

                // Gentle depth scale
                val scale = 1f - 0.05f * absPos
                view.scaleX = scale
                view.scaleY = scale

                // Smooth fade at the extreme edge so mirrored back is never visible
                view.alpha = if (absPos > 0.95f) 0f else 1f - 0.12f * absPos
            }
            position <= 1f -> {
                // Right page: As position goes 0 -> 1, turns to the right around right edge
                view.visibility = View.VISIBLE

                // CRITICAL: Cancels ViewPager2 horizontal translation so the spine stays pinned
                view.translationX = -position * width

                // Pivot on right edge
                view.pivotX = width
                view.pivotY = height * 0.5f

                // Rotate around Y-axis (0 deg to +90 deg)
                view.rotationY = 90f * position

                val absPos = abs(position)
                view.elevation = (1f - absPos) * 30f + 5f

                // Gentle depth scale
                val scale = 1f - 0.05f * absPos
                view.scaleX = scale
                view.scaleY = scale

                // Smooth fade at the extreme edge
                view.alpha = if (absPos > 0.95f) 0f else 1f - 0.12f * absPos
            }
            else -> {
                // Completely off-screen to the right
                view.alpha = 0f
                view.visibility = View.INVISIBLE
                view.translationX = 0f
                view.rotationY = 0f
            }
        }
    } catch (e: Throwable) {
        // Safe fallback
    }
}
}
