package com.example.mycustomtabsapp
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View


class MainActivity : AppCompatActivity() {

    private lateinit var customTabLayout: CustomTabLayout
    private lateinit var customViewPager: CustomViewPager
    private val tabs = listOf(
        "头条", "推荐", "7x24", "股票", "期货", "美股", "港股", "日历", "专栏", "视频", "社区"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rightOverlay = findViewById<View>(R.id.right_overlay)
        val screenWidth = resources.displayMetrics.widthPixels
        val overlayWith = (screenWidth * 0.25).toInt()

        rightOverlay.layoutParams.width = overlayWith
        rightOverlay.requestLayout()

        customTabLayout = findViewById(R.id.custom_tab_layout)
        customViewPager = findViewById(R.id.custom_view_pager)

        customTabLayout.setTabs(tabs)

        tabs.forEachIndexed { index, title ->
            val pageView = TextView(this).apply {
                textSize = 30f
                gravity = Gravity.CENTER
//                setBackgroundColor(colors[index])
            }
            customViewPager.addView(pageView)
        }

        customViewPager.onPageChangedListener = { newPageIndex ->
            customTabLayout.selectTab(newPageIndex, true)
        }

        customTabLayout.onTabSelectedListener = { position ->
            val targetScrollX = customViewPager.width * position
            customViewPager.scrollTo(targetScrollX, 0)
        }
    }
}