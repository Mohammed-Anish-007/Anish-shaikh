package com.anish.jarvis

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/** Animated "arc reactor" core. Bright and fast while Jarvis listens, calm while idle. */
class ReactorView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var active = false
        set(v) {
            field = v
            invalidate()
        }

    private var t = 0f
    private var spin = 0f
    private val anim = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 3000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            val v = it.animatedValue as Float
            val dt = if (v < t) v + 1 - t else v - t
            spin += dt * (if (active) 360f else 70f)
            t = v
            invalidate()
        }
    }

    private val cyan = Color.parseColor("#22D3EE")
    private val indigo = Color.parseColor("#6366F1")

    private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
    private val core = Paint(Paint.ANTI_ALIAS_FLAG)
    private val seg = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val thin = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val r1 = RectF()
    private val r2 = RectF()
    private val r3 = RectF()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        anim.start()
    }

    override fun onDetachedFromWindow() {
        anim.cancel()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val cx = w / 2f
        val cy = h / 2f
        val r = min(w, h) / 2f
        glow.shader = RadialGradient(cx, cy, r, intArrayOf(Color.argb(150, 34, 211, 238), Color.argb(0, 34, 211, 238)),
            floatArrayOf(0.25f, 1f), Shader.TileMode.CLAMP)
        core.shader = RadialGradient(cx, cy, r * 0.26f, intArrayOf(Color.WHITE, cyan, Color.argb(0, 34, 211, 238)),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        r1.set(cx - r * 0.78f, cy - r * 0.78f, cx + r * 0.78f, cy + r * 0.78f)
        r2.set(cx - r * 0.60f, cy - r * 0.60f, cx + r * 0.60f, cy + r * 0.60f)
        r3.set(cx - r * 0.44f, cy - r * 0.44f, cx + r * 0.44f, cy + r * 0.44f)
        seg.strokeWidth = r * 0.07f
        arc.strokeWidth = r * 0.035f
        thin.strokeWidth = r * 0.012f
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f
        val wave = (sin(t * 2 * PI * (if (active) 2 else 1)).toFloat() + 1f) / 2f
        val strength = if (active) 0.65f + 0.35f * wave else 0.25f + 0.15f * wave

        // soft glow
        glow.alpha = (255 * strength).toInt()
        canvas.drawCircle(cx, cy, r, glow)

        // outer segmented ring (clockwise)
        seg.color = cyan
        seg.alpha = (90 + 140 * strength).toInt()
        canvas.save()
        canvas.rotate(spin, cx, cy)
        for (i in 0 until 12) canvas.drawArc(r1, i * 30f, 17f, false, seg)
        canvas.restore()

        // middle arcs (counter-clockwise)
        arc.color = indigo
        arc.alpha = (120 + 120 * strength).toInt()
        canvas.save()
        canvas.rotate(-spin * 1.6f, cx, cy)
        for (i in 0 until 3) canvas.drawArc(r2, i * 120f, 75f, false, arc)
        canvas.restore()

        // inner thin ring
        thin.color = cyan
        thin.alpha = (80 + 120 * strength).toInt()
        canvas.drawOval(r3, thin)

        // pulsing core
        val coreR = r * 0.26f * (0.92f + 0.12f * wave * (if (active) 1f else 0.4f))
        core.alpha = (170 + 85 * strength).toInt().coerceAtMost(255)
        canvas.drawCircle(cx, cy, coreR, core)
    }
}
