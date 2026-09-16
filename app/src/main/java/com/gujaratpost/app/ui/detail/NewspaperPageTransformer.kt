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

            // Density-aware 3D perspective camera distance (prevents near-plane clipping/distortion)
            view.cameraDistance = 12000f * view.resources.displayMetrics.density

            when {
                position < -1f -> {
                    // Completely off-screen to the left
                    view.alpha = 0f
                    view.visibility = View.INVISIBLE
                    view.translationX = 0f
                    view.rotationY = 0f
                }
                position <= 0f -> {
                    // Left page: Turning around left spine (pivotX = 0)
                    view.visibility = View.VISIBLE

                    // Pin spine in place to cancel default ViewPager2 slide
                    view.translationX = -position * width

                    view.pivotX = 0f
                    view.pivotY = height * 0.5f

                    // Rotate 0 deg -> -90 deg
                    view.rotationY = 90f * position

                    val absPos = abs(position)
                    // High elevation so it turns above the underlying page
                    view.elevation = (1f - absPos) * 20f + 10f

                    // Gentle depth scale
                    val scale = 1f - 0.05f * absPos
                    view.scaleX = scale
                    view.scaleY = scale

                    // Smooth fade near 90 degrees so back is not visible
                    view.alpha = if (absPos > 0.96f) 0f else 1f - 0.15f * absPos
                }
                position <= 1f -> {
                    // Right page: Resting underneath, revealed as top page flips
                    view.visibility = View.VISIBLE

                    // Pin in place underneath turning page
                    view.translationX = -position * width

                    view.pivotX = 0f
                    view.pivotY = height * 0.5f
                    view.rotationY = 0f

                    // Base elevation below the turning page
                    view.elevation = 2f

                    // Subtle depth scale
                    val scale = 0.95f + 0.05f * (1f - position)
                    view.scaleX = scale
                    view.scaleY = scale

                    // Visible with subtle depth dimming
                    view.alpha = 1f - 0.2f * position
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
            view.alpha = 1f
            view.translationX = 0f
            view.rotationY = 0f
            view.scaleX = 1f
            view.scaleY = 1f
        }
    }
}
