package isetb.prjtinteg.gestiondepenses

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo)
        val appName = findViewById<TextView>(R.id.app_name)


        val logoFade = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)
        val logoMove = ObjectAnimator.ofFloat(logo, "translationY", 100f, 0f)

        val nameFade = ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f)
        val nameMove = ObjectAnimator.ofFloat(appName, "translationY", 50f, 0f)

        AnimatorSet().apply {
            playTogether(logoFade, logoMove, nameFade, nameMove)
            duration = 1200
            start()
        }


        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2800)
    }
}