package com.gujaratpost.app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.gujaratpost.app.databinding.ActivitySplashBinding
import com.gujaratpost.app.ui.MainActivity
import com.gujaratpost.app.ui.detail.ArticleDetailActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashHandler = Handler(Looper.getMainLooper())
    private var isNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startSplashAnimations()
        scheduleNavigation()
    }

    private fun startSplashAnimations() {
        // Logo container card: Initial state
        binding.cardSplashLogo.alpha = 0f
        binding.cardSplashLogo.scaleX = 0.85f
        binding.cardSplashLogo.scaleY = 0.85f

        // Tagline: Initial state
        binding.tvSplashTagline.alpha = 0f
        binding.tvSplashTagline.translationY = 24f

        // Discreet progress indicator: Initial state
        binding.progressSplash.alpha = 0f

        // Discreet footer: Initial state
        binding.tvSplashFooter.alpha = 0f

        // Animate Logo Container Card (Fade in + Overshoot scale)
        binding.cardSplashLogo.animate()
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(950)
            .setInterpolator(OvershootInterpolator(1.25f))
            .start()

        // Animate Tagline with subtle delay and decelerate
        binding.tvSplashTagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(350)
            .setDuration(700)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Animate Loading Indicator & Footer softly
        binding.progressSplash.animate()
            .alpha(1f)
            .setStartDelay(650)
            .setDuration(500)
            .start()

        binding.tvSplashFooter.animate()
            .alpha(0.85f)
            .setStartDelay(650)
            .setDuration(500)
            .start()
    }

    private fun scheduleNavigation() {
        splashHandler.postDelayed({
            navigateToNextScreen()
        }, SPLASH_DURATION_MS)
    }

    private fun navigateToNextScreen() {
        if (isNavigated || isFinishing || isDestroyed) return
        isNavigated = true

        val incomingData = intent?.data
        val incomingAction = intent?.action
        val incomingExtras = intent?.extras

        // Deep link routing: check if incoming intent targets an article
        val targetIntent = if (incomingAction == Intent.ACTION_VIEW && incomingData != null) {
            val path = incomingData.path.orEmpty()
            if (path.startsWith("/news") || path.startsWith("/article")) {
                Intent(this, ArticleDetailActivity::class.java).apply {
                    action = incomingAction
                    data = incomingData
                    incomingExtras?.let { putExtras(it) }
                }
            } else {
                Intent(this, MainActivity::class.java).apply {
                    action = incomingAction
                    data = incomingData
                    incomingExtras?.let { putExtras(it) }
                }
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                action = incomingAction
                data = incomingData
                incomingExtras?.let { putExtras(it) }
            }
        }

        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(targetIntent)
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        splashHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SPLASH_DURATION_MS = 1800L
    }
}
