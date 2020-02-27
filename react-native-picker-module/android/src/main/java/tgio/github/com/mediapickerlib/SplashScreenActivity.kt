package tgio.github.com.mediapickerlib

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View

class SplashScreenActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed({
            startActivity(Intent(this, NativeActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }, 700)
    }
}