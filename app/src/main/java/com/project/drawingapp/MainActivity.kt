package com.project.drawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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

    // launcher to pick image from gallery
    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null){
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
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
        val permission = Manifest.permission.READ_MEDIA_IMAGES

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                Toast.makeText(this, "Gallery permission already granted.", Toast.LENGTH_SHORT).show()
                showGallery()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Optional: Explain to the user why this permission is needed (you can just request directly too)
                requestPermissions.launch(arrayOf(permission)) // OR show a custom dialog here if you want
            }
            else -> {
                // First time request or "Don't ask again" was not set
                requestPermissions.launch(arrayOf(permission))
            }
        }
    }

    private fun showGallery() {
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

}