package com.project.drawingapp

import android.R.attr.onClick
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var drawingView: DrawingView
    private lateinit var btnBrush: Button
    private lateinit var btnRed: Button
    private lateinit var btnBlue: Button
    private lateinit var btnGreen: Button
    private lateinit var btnBlack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBlack = findViewById(R.id.btn_black)
        btnBlue = findViewById(R.id.btn_blue)
        btnRed = findViewById(R.id.btn_red)
        btnGreen = findViewById(R.id.btn_green)

        drawingView = findViewById(R.id.drawing_view)
        drawingView.changeBrushSize(5.toFloat())
        btnBrush = findViewById(R.id.btn_brush)
        btnBrush.setOnClickListener {
            showBrushDialog()
        }

        btnGreen.setOnClickListener(this)
        btnBlack.setOnClickListener(this)
        btnBlue.setOnClickListener(this)
        btnRed.setOnClickListener(this)
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

    override fun onClick(view: View?) {
        when (view?.id){
            R.id.btn_red->{
                drawingView.setBrushColor(Color.RED)
            }
            R.id.btn_green->{
                drawingView.setBrushColor(Color.GREEN)
            }
            R.id.btn_blue->{
                drawingView.setBrushColor(Color.BLUE)
            }
            R.id.btn_black->{
                drawingView.setBrushColor(Color.BLACK)
            }
        }
    }
}