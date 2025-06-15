package com.project.drawingapp

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var btnBrush: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        drawingView = findViewById(R.id.drawing_view)
        drawingView.changeBrushSize(20.toFloat())
        btnBrush = findViewById(R.id.btn_brush)
        btnBrush.setOnClickListener {
            showBrushDialog()
        }
    }

    private fun showBrushDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush)

        val seekBarProgress = brushDialog.findViewById<SeekBar>(R.id.dialog_brushSize_seekBar)
        val showProgressTxt = brushDialog.findViewById<TextView>(R.id.brushSize_text)

        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                drawingView.changeBrushSize(progress.toFloat())
                showProgressTxt.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        brushDialog.show()

        // 📏 Set width to 90% of screen width
        brushDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
    }


}