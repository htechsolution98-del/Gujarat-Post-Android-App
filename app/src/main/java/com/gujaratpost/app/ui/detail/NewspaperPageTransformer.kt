package com.gujaratpost.app.ui.detail

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * NewspaperPageTransformer gives a realistic 3D newspaper page turn / flip effect
 * when swiping horizontally between news articles.
 *
 * It applies 3D Y-axis rotation with proper camera perspective, subtle depth scaling,
 * and elevation transitions so the user feels like turning pages of a newspaper.
 */
class NewspaperPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        val width = view.width.toFloat()
        val height = view.height.toFloat()

        // Perspective camera distance to prevent near-plane clipping
        view.cameraDistance = 16000f * view.resources.displayMetrics.density

        when {
            position < -1f -> {
                // Way off-screen to the left
                view.alpha = 0f
            }
            position <= 0f -> {
                // Page turning to the left (current page turning away or previous settling)
                view.alpha = 1f - 0.15f * abs(position)
                view.pivotX = 0f
                view.pivotY = height * 0.5f
                view.rotationY = 55f * position

                val scale = 0.94f + (1f - 0.94f) * (1f - abs(position))
                view.scaleX = scale
                view.scaleY = scale
                view.elevation = (1f - abs(position)) * 10f
            }
            position <= 1f -> {
                // Page coming from the right (incoming next article or swiping back right)
                view.alpha = 1f - 0.15f * abs(position)
                view.pivotX = width
                view.pivotY = height * 0.5f
                view.rotationY = 55f * position

                val scale = 0.94f + (1f - 0.94f) * (1f - abs(position))
                view.scaleX = scale
                view.scaleY = scale
                view.elevation = (1f - abs(position)) * 10f
            }
            else -> {
                // Way off-screen to the right
                view.alpha = 0f
            }
        }
    }
}
