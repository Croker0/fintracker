package ru.fintracker.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import ru.fintracker.R
import kotlin.math.min

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Slice(val label: String, val valueMinor: Long, val color: Int)

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(context, R.color.text_secondary)
        textSize = 11f * scaledDensity
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(context, R.color.text_primary)
        textSize = 17f * scaledDensity
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private var slices: List<Slice> = emptyList()
    private var centerTitle: String = "Расходы"
    private var centerValue: String = "0 ₽"
    private var progress: Float = 1f
    private var animator: ValueAnimator? = null

    fun setData(slices: List<Slice>, title: String, value: String) {
        this.slices = slices.filter { it.valueMinor > 0L }
        centerTitle = title
        centerValue = value
        contentDescription = buildString {
            append(title).append(": ").append(value)
            if (this@DonutChartView.slices.isNotEmpty()) {
                append(". ")
                append(this@DonutChartView.slices.joinToString { "${it.label}" })
            }
        }
        animateIn()
    }

    private fun animateIn() {
        animator?.cancel()
        progress = 0f
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 650L
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (160 * density).toInt()
        val width = resolveSize(desired, widthMeasureSpec)
        val height = resolveSize(desired, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val stroke = min(width, height) * 0.14f
        arcPaint.strokeWidth = stroke

        val radius = min(width, height) / 2f - stroke / 2f - 2f * density
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        val total = slices.sumOf { it.valueMinor }
        if (total <= 0L) {
            arcPaint.color = ContextCompat.getColor(context, R.color.chart_empty)
            canvas.drawArc(rect, -90f, 359.9f, false, arcPaint)
        } else {
            var angle = -90f
            slices.forEach { slice ->
                val fraction = slice.valueMinor.toFloat() / total
                val sweep = (360f * fraction * progress - GAP_DEGREES).coerceAtLeast(0f)
                if (sweep > 0f) {
                    arcPaint.color = slice.color
                    canvas.drawArc(rect, angle + GAP_DEGREES / 2f, sweep, false, arcPaint)
                }
                angle += 360f * fraction * progress
            }
        }

        canvas.drawText(centerTitle, centerX, centerY - 2f * density, titlePaint)
        canvas.drawText(centerValue, centerX, centerY + 18f * density, valuePaint)
    }

    private companion object {
        const val GAP_DEGREES = 2.5f
    }
}
