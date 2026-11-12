package ru.fintracker.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import ru.fintracker.R
import kotlin.math.ceil
import kotlin.math.min

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Bar(val label: String, val incomeMinor: Long, val expenseMinor: Long)

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
        color = ContextCompat.getColor(context, R.color.chart_grid)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 9f * scaledDensity
        color = ContextCompat.getColor(context, R.color.text_secondary)
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 12f * scaledDensity
        color = ContextCompat.getColor(context, R.color.text_secondary)
    }

    private val expenseColor = ContextCompat.getColor(context, R.color.expense)
    private var bars: List<Bar> = emptyList()
    private var progress: Float = 1f
    private var animator: ValueAnimator? = null

    fun setData(bars: List<Bar>, description: String = "Динамика расходов") {
        this.bars = bars
        contentDescription = description
        animateIn()
    }

    private fun animateIn() {
        animator?.cancel()
        progress = 0f
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val left = 2f * density
        val right = width - 2f * density
        val top = 6f * density
        val bottom = height - 22f * density
        val chartHeight = bottom - top

        if (chartHeight <= 0f || width <= 0) return

        if (bars.isEmpty() || bars.all { it.expenseMinor == 0L }) {
            canvas.drawText(
                "Нет данных за период",
                width / 2f,
                height / 2f,
                emptyPaint
            )
            return
        }

        val maxValue = bars.maxOf { it.expenseMinor }.coerceAtLeast(1L)

        for (i in 0..3) {
            val y = bottom - chartHeight * i / 3f
            canvas.drawLine(left, y, right, y, gridPaint)
        }

        val slot = (right - left) / bars.size
        val barWidth = min(slot * 0.55f, 14f * density)

        bars.forEachIndexed { index, bar ->
            drawBar(
                canvas = canvas,
                centerX = left + slot * (index + 0.5f),
                barWidth = barWidth,
                value = bar.expenseMinor,
                maxValue = maxValue,
                bottom = bottom,
                chartHeight = chartHeight,
                color = expenseColor
            )
        }

        val labelStep = ceil(bars.size / 6.0).toInt().coerceAtLeast(1)
        bars.forEachIndexed { index, bar ->
            if (index % labelStep == 0 || index == bars.lastIndex) {
                canvas.drawText(
                    bar.label.take(4),
                    left + slot * (index + 0.5f),
                    height - 6f * density,
                    labelPaint
                )
            }
        }
    }

    private fun drawBar(
        canvas: Canvas,
        centerX: Float,
        barWidth: Float,
        value: Long,
        maxValue: Long,
        bottom: Float,
        chartHeight: Float,
        color: Int
    ) {
        if (value <= 0L) return
        val height = chartHeight * (value.toFloat() / maxValue) * progress
        if (height < 1f) return
        barPaint.color = color
        val half = barWidth / 2f
        canvas.drawRoundRect(
            RectF(centerX - half, bottom - height, centerX + half, bottom),
            half,
            half,
            barPaint
        )
    }
}
