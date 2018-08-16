package sashiro.com.trimmingview.sample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import sashiro.com.trimmingview.ext.dp2px
import sashiro.com.trimmingview.ext.getCompColor
import sashiro.com.trimmingview.model.TrimmingViewConfig

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        trimmingView.config = TrimmingViewConfig.Builder(baseContext)
                .isBackgroundShow(true)
                .setRatio(16 / 9f)
                .showAnim(true)
                .setBorderWidth(baseContext.dp2px(2f).toFloat())
                .setBorderColor(baseContext.getCompColor(R.color.colorAccent))
                .build()
        rotateLBtn.setOnClickListener {
            trimmingView.turnClockwise()
        }

        rotateRBtn.setOnClickListener {
            trimmingView.turnAnticlockwise()
        }
        goResultBtn.setOnClickListener {
            val result = trimmingView.getResult(2668, 2000)
            val intent = Intent(this, TrimmingResultActivity::class.java)
                    .putExtra("left", result.trimmingRect.left)
                    .putExtra("right", result.trimmingRect.right)
                    .putExtra("top", result.trimmingRect.top)
                    .putExtra("bottom", result.trimmingRect.bottom)
                    .putExtra("angle", trimmingView.getCurrentAngle())
            startActivity(intent)
        }
        resetBtn.setOnClickListener {
            trimmingView.reset()
        }
    }
}
