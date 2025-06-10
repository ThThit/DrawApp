package com.project.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.graphics.Path
import androidx.core.graphics.createBitmap

class DrawingView (context: Context, attrs: AttributeSet): View(context, attrs){
    // drawing path
    private lateinit var drawPath: FingerPath

    // what to draw
    private lateinit var canvasPaint: Paint

    // how to draw
    private lateinit var drawPaint: Paint
    private val color = Color.BLACK
    private lateinit var  canvas: Canvas
    private lateinit var canvasBitMap: Bitmap
    private var brushSize: Float = 0.toFloat()

    init {
        setUpDrawing()
    }

    //
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitMap = createBitmap(w, h)
        canvas = Canvas(canvasBitMap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitMap, 0f, 0f, drawPaint)
        if (!drawPath.isEmpty){
            drawPaint.strokeWidth = drawPaint.strokeWidth
            drawPaint.color = drawPath.color
            canvas.drawPath(drawPath, drawPaint) // drawing path on canvas
        }
    }

    private fun setUpDrawing(){
        drawPaint = Paint()
        drawPath = FingerPath(color, brushSize)
        drawPaint.color = color
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
        brushSize = 20.toFloat()
    }

    internal inner class FingerPath(val color: Int, val brushThickness: Float): Path()
}