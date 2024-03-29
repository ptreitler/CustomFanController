package com.example.android.customfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class FanSpeed(val labelResId: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius = 0.0f // Radius of the circle
    private var fanSpeed = FanSpeed.OFF // The active selection
    // Position variable which will be used to draw label and indicator circle position
    private val position = PointF(0.0f, 0.0f)

    private var fanSpeedOffColor = 0
    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSpeedHighColor = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedOffColor = getColor(R.styleable.DialView_fanColor0, 0)
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSpeedHighColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.labelResId)

        invalidate()

        return true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    private fun PointF.computeXYForSpeed(speed: FanSpeed, radius: Float) {
        // Angles are in radians
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + speed.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> fanSpeedOffColor
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSpeedHighColor
        }

        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        position.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(position.x, position.y, radius / 12, paint)

        val labelRadius = radius + RADIUS_OFFSET_LABEL
        FanSpeed.values().forEach {
            position.computeXYForSpeed(it, labelRadius)
            val label = resources.getString(it.labelResId)
            canvas.drawText(label, position.x, position.y, paint)
        }
    }
}