package com.example.mycustomtabsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val customTabLayout = findViewById<CustomTabLayout>(R.id.custom_tab_layout)

        val tabs = listOf(
            "关注", "推荐", "视频", "抗疫", "深圳", "热榜", "科技", "财经"
        )

        customTabLayout.setTabs(tabs)

    }
}