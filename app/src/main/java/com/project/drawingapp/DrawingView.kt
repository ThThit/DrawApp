package com.project.drawingapp

import android.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.graphics.Path
import android.util.TypedValue
import android.view.MotionEvent
import androidx.core.graphics.createBitmap

class DrawingView (context: Context, attrs: AttributeSet): View(context, attrs){
    // drawing path
    private lateinit var drawPath: FingerPath

    // what to draw
    private lateinit var canvasPaint: Paint

    // how to draw
    private lateinit var drawPaint: Paint
    private var currentColor: Int = Color.BLACK
    private lateinit var  canvas: Canvas
    private lateinit var canvasBitMap: Bitmap
    private var brushSize: Float = 0.toFloat()
    private var paths = mutableListOf<FingerPath>()

    init {
        setUpDrawing()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitMap = createBitmap(w, h)
        canvas = Canvas(canvasBitMap)
    }

    // called by the system when user touches the screen
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            // when user put finger on the screen
            MotionEvent.ACTION_DOWN -> {
                drawPath.color = currentColor
                drawPath.brushThickness = brushSize
                drawPath.reset() // rest the draw path initial point
                drawPath.moveTo(touchX!!, touchY!!)
            }
            // when user starts to move the finger; continuously when touching
            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX!!, touchY!!)
            }
            // when user picks up the finger
            MotionEvent.ACTION_UP -> {
                drawPath = FingerPath(currentColor, brushSize)
                paths.add(drawPath)
            }
            else -> return false
        }
        invalidate() // refreshing the layout for changes
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitMap, 0f, 0f, drawPaint)

        for (path in paths){
            drawPaint.strokeWidth = path.brushThickness
            drawPaint.color = path.color
            canvas.drawPath(path, drawPaint) // drawing path on canvas
        }
    }

    private fun setUpDrawing(){
        drawPaint = Paint()
        drawPath = FingerPath(currentColor, brushSize)
        drawPaint.color = currentColor
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
        brushSize = 5.toFloat()
    }

    fun changeBrushSize(newSize: Float){
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize, resources.displayMetrics
        )
        drawPaint.strokeWidth = brushSize
    }

    fun setBrushColor(colorInt: Int){
        currentColor = colorInt
        drawPaint.color = currentColor
    }



    internal inner class FingerPath(var color: Int, var brushThickness: Float): Path()
}