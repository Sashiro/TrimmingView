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
            val rect = it.getParcelableExtra<Rect>("rect")
            val angle = it.getFloatExtra("angle", 0f)

            trimmingResultView.setResult(2668, 2000,
                    TrimmingResult(rect, angle))
        }

    }
}