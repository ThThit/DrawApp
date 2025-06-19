package com.project.drawingapp

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var drawingView: DrawingView
    private lateinit var btnBrush: Button
    private lateinit var btnRed: Button
    private lateinit var btnBlue: Button
    private lateinit var btnGreen: Button
    private lateinit var btnBlack: Button
    private lateinit var btnUndo: Button
    private lateinit var btnPickColor: Button
    private lateinit var btnGallery: Button

    // permission request
    val requestPermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { (permission, isGranted) ->
                when (permission){
                    Manifest.permission.READ_MEDIA_IMAGES -> {
                        if (isGranted){
                            Toast.makeText(this, "Gallery permission granted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

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
        btnUndo = findViewById(R.id.btn_undo)
        btnPickColor = findViewById(R.id.btn_color_pick)
        btnGallery = findViewById(R.id.btn_gallery)

        drawingView = findViewById(R.id.drawing_view)
        drawingView.changeBrushSize(5.toFloat())
        btnBrush = findViewById(R.id.btn_brush)
        btnBrush.setOnClickListener {
            showBrushDialog()
        }

        btnUndo.setOnClickListener(this)
        btnGreen.setOnClickListener(this)
        btnBlack.setOnClickListener(this)
        btnBlue.setOnClickListener(this)
        btnRed.setOnClickListener(this)
        btnPickColor.setOnClickListener(this)
        btnGallery.setOnClickListener(this)
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

    // button actions
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
            R.id.btn_undo->{
                drawingView.undoPath()
            }
            R.id.btn_color_pick->{
                showColorPicker()
            }
            R.id.btn_gallery->{
                requestStoragePermission()
            }
        }
    }

    // color picker dialog
    private fun showColorPicker(){
        val dialog = AmbilWarnaDialog(this, Color.BLUE, object: OnAmbilWarnaListener{
            override fun onCancel(dialog: AmbilWarnaDialog?) {

            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingView.setBrushColor(color)
            }
        })
        dialog.show()
    }

    // request storage permission
    private fun requestStoragePermission(){
        // check if the permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Gallery permission already granted.", Toast.LENGTH_SHORT).show()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
            showRationDialog()
        } else {
            requestPermissions.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
        }
    }

    private fun showRationDialog(){
        val permission = Manifest.permission.READ_MEDIA_IMAGES

        AlertDialog.Builder(this)
            .setTitle("Gallery Permission")
            .setMessage("Need storage permission to access gallery")
            .setPositiveButton(android.R.string.ok) {dialog, _->
                requestPermissions.launch(arrayOf(permission))
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _->
                Toast.makeText(this, "Gallery access denied!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }
}