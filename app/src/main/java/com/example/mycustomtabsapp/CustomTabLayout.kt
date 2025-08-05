package com.example.mycustomtabsapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
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

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var indicatorHeight = 10f
    private var indicatorLeft = 0f
    private var indicatorRight = 0f

    var onTabSelectedListener: ((position: Int) -> Unit)? = null

    init {
        setWillNotDraw(false)
        indicatorPaint.apply {
            style = Paint.Style.FILL
            // 初始颜色会被 updateTabStyles 覆盖，但保留一个默认值是好习惯
            color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        }
        tabContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }
        addView(tabContainer)
    }

    fun setTabs(titles: List<String>) {
        this.tabTitles = titles
        tabContainer.removeAllViews()

        val screenWidth = context.resources.displayMetrics.widthPixels
        val tabWidth = screenWidth / 5

        titles.forEachIndexed { index, title ->
            val tabView = TextView(context).apply {
                text = title
                textSize = 16f
                gravity = Gravity.CENTER
                setOnClickListener { selectTab(index) }
            }
            val layoutParams = LinearLayout.LayoutParams(tabWidth, LinearLayout.LayoutParams.MATCH_PARENT)
            tabContainer.addView(tabView, layoutParams)
        }

        selectTab(0, animate = false)
        post {
            updateIndicatorPosition(0)
            invalidate()
        }
    }

    fun selectTab(position: Int, animate: Boolean = true) {
        if (position < 0 || position >= tabTitles.size) return

        val oldPosition = currentTabPosition
        currentTabPosition = position
        updateTabStyles() // 调用样式更新方法

        if (animate) {
            startIndicatorAnimation(oldPosition, currentTabPosition)
        } else {
            updateIndicatorPosition(currentTabPosition)
            invalidate()
        }

        centerTab(position)
        onTabSelectedListener?.invoke(position)
    }

    /**
     * 更新所有 Tab 的文本样式和滑动条颜色
     */
    private fun updateTabStyles() {
        tabContainer.children.forEachIndexed { index, view ->
            if (view is TextView) {
                if (index == currentTabPosition) {
                    val selectedColor = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
                    view.setTextColor(selectedColor)
                    view.textSize = 20f
                    indicatorPaint.color = selectedColor
                } else {
                    view.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    view.textSize = 16f
                }
            }
        }
        invalidate() // 强制重绘以应用新颜色
    }

    private fun centerTab(position: Int) {
        val tabView = tabContainer.getChildAt(position) ?: return
        val targetScrollX = (tabView.left + tabView.right) / 2 - width / 2
        smoothScrollTo(targetScrollX, 0)
    }

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
                indicatorLeft = startLeft + (endLeft - startLeft) * fraction
                indicatorRight = startRight + (endRight - startRight) * fraction
                invalidate()
            }
            start()
        }
    }

    private fun updateIndicatorPosition(position: Int) {
        val tabView = tabContainer.getChildAt(position) ?: return
        indicatorLeft = tabView.left.toFloat()
        indicatorRight = tabView.right.toFloat()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (tabContainer.childCount > 0) {
            canvas.drawRect(indicatorLeft, height - indicatorHeight, indicatorRight, height.toFloat(), indicatorPaint)
        }
    }
}