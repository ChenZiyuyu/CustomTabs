package com.example.mycustomtabsapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children

class CustomTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private val tabContainer: LinearLayout
    private var tabTitles: List<String> = emptyList()
    private var currentTabPosition = 0

    // --- 指示器相关属性 ---
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var indicatorHeight = 10f
    private var indicatorLeft = 0f
    private var indicatorRight = 0f

    /**
     * Tab 选中事件的回调。
     * 使用函数类型替代接口，这是更 Kotlin-idiomatic 的方式。
     * (Int) -> Unit 表示一个接收一个 Int 参数且无返回值的函数。
     */
    var onTabSelectedListener: ((position: Int) -> Unit)? = null

    init {
        // 告诉 ViewGroup 它需要调用 onDraw/dispatchDraw
        setWillNotDraw(false)

        // 初始化指示器画笔
        indicatorPaint.apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        }

        // 初始化 LinearLayout 容器
        tabContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }
        addView(tabContainer)
    }

    /**
     * 设置要显示的标签列表
     */
    fun setTabs(titles: List<String>) {
        this.tabTitles = titles
        tabContainer.removeAllViews()

        titles.forEachIndexed { index, title ->
            val tabView = TextView(context).apply {
                text = title
                textSize = 16f
                setPadding(40, 20, 40, 20)
                setOnClickListener { selectTab(index) }
            }
            tabContainer.addView(tabView)
        }

        // 默认选中第一个，并初始化指示器位置
        selectTab(0, animate = false)
        post {
            updateIndicatorPosition(0)
            invalidate()
        }
    }

    /**
     * 选中一个标签
     * @param position 要选中的标签索引
     * @param animate 是否需要动画
     */
    fun selectTab(position: Int, animate: Boolean = true) {
        if (position < 0 || position >= tabTitles.size) return

        val oldPosition = currentTabPosition
        currentTabPosition = position
        updateTabStyles()

        if (animate) {
            startIndicatorAnimation(oldPosition, currentTabPosition)
        } else {
            // 如果不需要动画，立即更新指示器位置并重绘
            updateIndicatorPosition(currentTabPosition)
            invalidate()
        }

        // 触发回调
        onTabSelectedListener?.invoke(position)
    }

    /**
     * 更新所有 Tab 的文本样式（选中/未选中）
     */
    private fun updateTabStyles() {
        tabContainer.children.forEachIndexed { index, view ->
            if (view is TextView) {
                val colorRes = if (index == currentTabPosition) {
                    android.R.color.holo_blue_dark
                } else {
                    android.R.color.black
                }
                view.setTextColor(ContextCompat.getColor(context, colorRes))
            }
        }
    }

    /**
     * 启动指示器位置变化的动画
     */
    private fun startIndicatorAnimation(fromPosition: Int, toPosition: Int) {
        val fromView = tabContainer.getChildAt(fromPosition) ?: return
        val toView = tabContainer.getChildAt(toPosition) ?: return

        val startLeft = fromView.left.toFloat()
        val startRight = fromView.right.toFloat()
        val endLeft = toView.left.toFloat()
        val endRight = toView.right.toFloat()

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 250
            addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                // 通过线性插值计算当前指示器的左右位置
                indicatorLeft = startLeft + (endLeft - startLeft) * fraction
                indicatorRight = startRight + (endRight - startRight) * fraction
                invalidate() // 请求重绘
            }
            start()
        }
    }

    /**
     * 直接更新指示器位置（无动画）
     */
    private fun updateIndicatorPosition(position: Int) {
        val tabView = tabContainer.getChildAt(position) ?: return
        indicatorLeft = tabView.left.toFloat()
        indicatorRight = tabView.right.toFloat()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        // 绘制子View后，在它们之上绘制我们的指示器
        if (tabContainer.childCount > 0) {
            canvas.drawRect(indicatorLeft, height - indicatorHeight, indicatorRight, height.toFloat(), indicatorPaint)
        }
    }
}