package com.example.myapplicationx.ui.settings.companySignature

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CompanySignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // Drawing components
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mPath = Path()
    private val mPaint = Paint().apply {
        color = 0xFF000000.toInt() // Black
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val mBitmapPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }
    
    // Touch tracking variables
    private var mX = 0f
    private var mY = 0f
    private val TOUCH_TOLERANCE = 4f
    
    // Create bitmap when size is known
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Only create a new blank bitmap if one doesn't exist
        if (w > 0 && h > 0 && mBitmap == null) {
            createBlankBitmap(w, h)
        }
    }
    
    private fun createBlankBitmap(width: Int, height: Int) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
    }

    // Set an existing signature bitmap
    fun setSignatureBitmap(bitmap: Bitmap) {
        if (bitmap.width <= 0 || bitmap.height <= 0) return
        
        mBitmap = if (!bitmap.isMutable) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap
        }
        mCanvas = Canvas(mBitmap!!)
        invalidate()
    }

    // Get the current signature as a bitmap
    fun getSignatureBitmap(): Bitmap? {
        if (width <= 0 || height <= 0) return null
        
        // If no bitmap exists yet, create one with current dimensions
        if (mBitmap == null) {
            createBlankBitmap(width, height)
        }
        
        return mBitmap?.copy(Bitmap.Config.ARGB_8888, true)
    }

    // Clear the signature
    fun clear() {
        mPath.reset()
        if (width > 0 && height > 0) {
            createBlankBitmap(width, height)
            invalidate()
        }
    }

    // Drawing methods
    private fun touchStart(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // Create a smooth curve by quadratic bezier
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        mPath.lineTo(mX, mY)
        
        // Commit the path to the bitmap
        mCanvas?.drawPath(mPath, mPaint)
        mPath.reset()
    }

    // Handle touch events
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mBitmap == null && width > 0 && height > 0) {
            createBlankBitmap(width, height)
        }
        
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    // Draw the signature
    override fun onDraw(canvas: Canvas) {
        // Set background if needed
        canvas.drawColor(0xFFFFFFFF.toInt()) // Optional: white background
        
        // Draw the saved bitmap
        mBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, mBitmapPaint)
        }
        
        // Draw current path being drawn
        canvas.drawPath(mPath, mPaint)
    }
}