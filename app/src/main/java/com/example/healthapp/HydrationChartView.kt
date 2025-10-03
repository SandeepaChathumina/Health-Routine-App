package com.example.healthapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HydrationChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var chartData = mutableListOf<ChartDataPoint>()
    private var maxValue = 6000f
    private var minValue = 0f
    
    // Colors
    private val primaryColor = Color.parseColor("#00BCD4")
    private val gridColor = Color.parseColor("#E0E0E0")
    private val textColor = Color.parseColor("#666666")
    private val backgroundColor = Color.parseColor("#FAFAFA")
    
    // Chart dimensions
    private val padding = 40f
    private val pointRadius = 6f
    private val lineWidth = 4f
    
    data class ChartDataPoint(
        val time: String,
        val value: Float,
        val label: String
    )

    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        // Line paint
        linePaint.color = primaryColor
        linePaint.strokeWidth = lineWidth
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeJoin = Paint.Join.ROUND
        
        // Point paint
        pointPaint.color = primaryColor
        pointPaint.style = Paint.Style.FILL
        
        // Text paint
        textPaint.color = textColor
        textPaint.textSize = 32f
        textPaint.textAlign = Paint.Align.CENTER
        
        // Grid paint
        gridPaint.color = gridColor
        gridPaint.strokeWidth = 1f
        gridPaint.style = Paint.Style.STROKE
    }
    
    fun setData(data: List<ChartDataPoint>) {
        chartData.clear()
        chartData.addAll(data)
        
        if (data.isNotEmpty()) {
            maxValue = data.maxOfOrNull { it.value }?.let { max(it, 1000f) } ?: 1000f
            minValue = 0f
        }
        
        invalidate()
    }
    
    fun addDataPoint(time: String, value: Float, label: String) {
        chartData.add(ChartDataPoint(time, value, label))
        
        if (value > maxValue) {
            maxValue = value
        }
        
        invalidate()
    }
    
    fun clearData() {
        chartData.clear()
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (chartData.isEmpty()) {
            drawEmptyState(canvas)
            return
        }
        
        val width = width.toFloat()
        val height = height.toFloat()
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        
        // Draw background
        canvas.drawColor(backgroundColor)
        
        // Draw grid
        drawGrid(canvas, chartWidth, chartHeight)
        
        // Draw chart line and points
        drawChart(canvas, chartWidth, chartHeight)
        
        // Draw labels
        drawLabels(canvas, chartWidth, chartHeight)
    }
    
    private fun drawEmptyState(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        
        textPaint.textSize = 36f
        textPaint.color = textColor
        canvas.drawText("No data yet", width / 2, height / 2, textPaint)
        
        textPaint.textSize = 28f
        canvas.drawText("Start drinking water to see your progress", width / 2, height / 2 + 50, textPaint)
    }
    
    private fun drawGrid(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        val gridLines = 5
        val stepY = chartHeight / gridLines
        
        for (i in 0..gridLines) {
            val y = padding + i * stepY
            canvas.drawLine(padding, y, padding + chartWidth, y, gridPaint)
        }
        
        // Draw vertical grid lines
        if (chartData.size > 1) {
            val stepX = chartWidth / (chartData.size - 1)
            for (i in chartData.indices) {
                val x = padding + i * stepX
                canvas.drawLine(x, padding, x, padding + chartHeight, gridPaint)
            }
        }
    }
    
    private fun drawChart(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        if (chartData.size < 2) return
        
        val path = Path()
        val points = mutableListOf<PointF>()
        
        // Calculate points
        for (i in chartData.indices) {
            val x = if (chartData.size == 1) {
                padding + chartWidth / 2
            } else {
                padding + (i * chartWidth / (chartData.size - 1))
            }
            
            val normalizedValue = (chartData[i].value - minValue) / (maxValue - minValue)
            val y = padding + chartHeight - (normalizedValue * chartHeight)
            
            points.add(PointF(x, y))
        }
        
        // Draw line
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        canvas.drawPath(path, linePaint)
        
        // Draw points
        for (point in points) {
            canvas.drawCircle(point.x, point.y, pointRadius, pointPaint)
        }
    }
    
    private fun drawLabels(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        if (chartData.isEmpty()) return
        
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
        
        // Draw time labels
        for (i in chartData.indices) {
            val x = if (chartData.size == 1) {
                padding + chartWidth / 2
            } else {
                padding + (i * chartWidth / (chartData.size - 1))
            }
            
            val y = padding + chartHeight + 30
            canvas.drawText(chartData[i].time, x, y, textPaint)
        }
        
        // Draw value labels on the right
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 20f
        
        val gridLines = 5
        val stepY = chartHeight / gridLines
        
        for (i in 0..gridLines) {
            val value = maxValue - (i * (maxValue - minValue) / gridLines)
            val y = padding + i * stepY + 6
            canvas.drawText("${value.toInt()}ml", padding + chartWidth + 10, y, textPaint)
        }
    }
}
