package sashiro.com.trimmingview.sample

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
                .isBackgroundShow(false)
                .setRatio(16 / 9f)
                .setBorderWidth(baseContext.dp2px(2f).toFloat())
                .setBorderColor(baseContext.getCompColor(R.color.colorAccent))
                .build()
        rotateLBtn.setOnClickListener {
            trimmingView.turnClockwise()
        }

        rotateRBtn.setOnClickListener {
            trimmingView.turnAnticlockwise()
        }
    }
}
