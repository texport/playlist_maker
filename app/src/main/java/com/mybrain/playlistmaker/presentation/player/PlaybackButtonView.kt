package com.mybrain.playlistmaker.presentation.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.mybrain.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }
    private val drawRect = RectF()
    private var playBitmap: Bitmap? = null
    private var pauseBitmap: Bitmap? = null
    private var isPlaying = false

    init {
        isClickable = true
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.PlaybackButtonView,
                defStyleAttr,
                0
            )
            val playResId = typedArray.getResourceId(
                R.styleable.PlaybackButtonView_playImage,
                0
            )
            val pauseResId = typedArray.getResourceId(
                R.styleable.PlaybackButtonView_pauseImage,
                0
            )
            typedArray.recycle()

            playBitmap = loadBitmap(playResId)
            pauseBitmap = loadBitmap(pauseResId)
        }
    }

    fun setPlaying(isPlaying: Boolean) {
        if (this.isPlaying == isPlaying) return
        this.isPlaying = isPlaying
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawRect.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        val bitmap = if (isPlaying) pauseBitmap else playBitmap
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, drawRect, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                true
            }
            MotionEvent.ACTION_UP -> {
                isPressed = false
                toggleState()
                performClick()
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun toggleState() {
        isPlaying = !isPlaying
        invalidate()
    }

    private fun loadBitmap(resId: Int): Bitmap? {
        if (resId == 0) return null
        val drawable = AppCompatResources.getDrawable(context, resId) ?: return null
        return drawable.toBitmap()
    }
}
