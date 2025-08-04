package com.example.mycustomtabsapp


import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var customTabLayout: CustomTabLayout
    private lateinit var customViewPager: CustomViewPager
    private val tabs = listOf(
        "关注", "推荐", "视频", "抗疫", "深圳", "热榜"
    )
//    private val colors = listOf(
//        Color.parseColor("#ffffff"), Color.parseColor("#ddffdd"),
//        Color.parseColor("#ddddff"), Color.parseColor("#ffffdd"),
//        Color.parseColor("#ffddff"), Color.parseColor("#ddffff")
//    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customTabLayout = findViewById(R.id.custom_tab_layout)
        customViewPager = findViewById(R.id.custom_view_pager)

        // 1. --- 配置 CustomTabLayout ---
        customTabLayout.setTabs(tabs)
        // 我们在后面会根据页面切换来更新 tab，所以可以先不设置监听器

        // 2. --- 向 CustomViewPager 添加页面 ---
        tabs.forEachIndexed { index, title ->
            val pageView = TextView(this).apply {
                text = title
                textSize = 30f
                gravity = Gravity.CENTER
//                setBackgroundColor(colors[index])
            }
            customViewPager.addView(pageView)
        }

        // 3. --- 核心：将两个自定义组件连接起来 ---

        // 当 CustomViewPager 页面切换完成时，更新 TabLayout
        customViewPager.onPageChangedListener = { newPageIndex ->
            // true 表示需要动画
            customTabLayout.selectTab(newPageIndex, true)
        }

        // 当点击 TabLayout 的标签时，切换 CustomViewPager
        customTabLayout.onTabSelectedListener = { position ->
            // 这里我们没有实现平滑滚动，所以是瞬时切换
            val targetScrollX = customViewPager.width * position
            customViewPager.scrollTo(targetScrollX, 0)
            // 更新一下内部索引
            // customViewPager.currentPageIndex = position (需要添加 setter 或方法来更新)
        }
    }
}