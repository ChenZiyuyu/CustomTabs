package com.example.mycustomtabsapp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.OverScroller
import kotlin.math.abs

class CustomViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs) {

    // OverScroller 用于处理滚动和投掷动画
    private val scroller = OverScroller(context)
    // 速度追踪器，用于计算手指滑动的速度
    private val velocityTracker = VelocityTracker.obtain()
    // 系统最小识别滑动距离
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    // 系统最大投掷速度
    private val maximumVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

    private var lastX = 0f
    private var isDragging = false
    var currentPageIndex = 0
        private set

    // 页面切换完成的回调
    var onPageChangedListener: ((Int) -> Unit)? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 测量所有子 View
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        // 设置自身的尺寸
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 将所有子 View 水平依次排列
        var childLeft = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(childLeft, 0, childLeft + child.measuredWidth, child.measuredHeight)
            childLeft += child.measuredWidth
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = ev.x
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = abs(ev.x - lastX)
                // 如果水平滑动距离大于系统最小识别距离，则拦截事件
                if (dx > touchSlop) {
                    isDragging = true
                    return true // 拦截触摸事件，自己处理
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        velocityTracker.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (lastX - event.x).toInt()
                // 跟随手指滚动
                scrollBy(dx, 0)
                lastX = event.x
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                // 计算 fling 速度
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                val velocityX = velocityTracker.xVelocity

                // 根据当前滚动位置和速度，判断应该滚动到哪个页面
                val targetIndex = if (abs(velocityX) > 500) { // 快速滑动
                    if (velocityX < 0) currentPageIndex + 1 else currentPageIndex - 1
                } else { // 慢速滑动，根据位置判断
                    (scrollX + width / 2) / width
                }.coerceIn(0, childCount - 1) // 确保索引不越界

                smoothScrollTo(targetIndex * width)

                // 如果目标页面变化了，更新当前页面索引并触发回调
                if (targetIndex != currentPageIndex) {
                    currentPageIndex = targetIndex
                    onPageChangedListener?.invoke(currentPageIndex)
                }

                velocityTracker.clear()
            }
        }
        return true
    }

    private fun smoothScrollTo(targetX: Int) {
        val dx = targetX - scrollX
        scroller.startScroll(scrollX, 0, dx, 0, 500) // 500ms 动画
        invalidate() // 触发 computeScroll
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            invalidate()
        }
    }
}