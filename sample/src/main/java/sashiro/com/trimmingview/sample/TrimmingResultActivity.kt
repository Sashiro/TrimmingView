package sashiro.com.trimmingview.sample

import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_trimming_result.*
import sashiro.com.trimmingview.model.TrimmingResult

class TrimmingResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimming_result)
        intent?.let {
            val left = it.getIntExtra("left", 0)
            val right = it.getIntExtra("right", 0)
            val top = it.getIntExtra("top", 0)
            val bottom = it.getIntExtra("bottom", 0)
            val angle = it.getFloatExtra("angle", 0f)

            trimmingResultView.setResult(2668, 2000,
                    TrimmingResult(Rect(left, top, right, bottom), angle))
        }

    }
}