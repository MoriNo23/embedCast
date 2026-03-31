package com.embedcast.tv

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION = 2500L
    }

    private lateinit var logoImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var loadingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logoImageView = findViewById(R.id.splashLogo)
        titleTextView = findViewById(R.id.splashTitle)
        loadingTextView = findViewById(R.id.splashLoading)

        setupInitialState()
        startAnimation()
        navigateToMain()
    }

    private fun setupInitialState() {
        logoImageView.alpha = 0f
        logoImageView.scaleX = 0.5f
        logoImageView.scaleY = 0.5f

        titleTextView.alpha = 0f
        titleTextView.translationY = 50f

        loadingTextView.alpha = 0f
    }

    private fun startAnimation() {
        val logoScaleX = ObjectAnimator.ofFloat(logoImageView, View.SCALE_X, 0.5f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(logoImageView, View.SCALE_Y, 0.5f, 1f)
        val logoAlpha = ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 0f, 1f)

        val titleAlpha = ObjectAnimator.ofFloat(titleTextView, View.ALPHA, 0f, 1f)
        val titleTranslation = ObjectAnimator.ofFloat(titleTextView, View.TRANSLATION_Y, 50f, 0f)

        val loadingAlpha = ObjectAnimator.ofFloat(loadingTextView, View.ALPHA, 0f, 1f)

        val logoSet = AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha)
            duration = 800
            interpolator = OvershootInterpolator(1.2f)
        }

        val titleSet = AnimatorSet().apply {
            playTogether(titleAlpha, titleTranslation)
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        val loadingSet = AnimatorSet().apply {
            playTogether(loadingAlpha)
            duration = 400
        }

        val fullAnimation = AnimatorSet().apply {
            playSequentially(logoSet, titleSet, loadingSet)
            startDelay = 200
        }

        fullAnimation.start()
    }

    private fun navigateToMain() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }, SPLASH_DURATION)
    }
}
