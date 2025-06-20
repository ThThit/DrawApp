package com.project.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.UUID


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
    private lateinit var btnSave: Button

    // permission request
    val requestPermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { (permission, isGranted) ->
                when (permission) {
                    Manifest.permission.READ_MEDIA_IMAGES -> {
                        if (isGranted) {
                            Toast.makeText(this, "Gallery permission granted", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }


    // launcher to pick image from gallery
    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    // set image
                    findViewById<ImageView>(R.id.gallery_img).setImageURI(selectedImageUri)
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
        btnSave = findViewById(R.id.btn_save)

        btnUndo.setOnClickListener(this)
        btnGreen.setOnClickListener(this)
        btnBlack.setOnClickListener(this)
        btnBlue.setOnClickListener(this)
        btnRed.setOnClickListener(this)
        btnPickColor.setOnClickListener(this)
        btnGallery.setOnClickListener(this)
        btnBrush.setOnClickListener(this)
        btnSave.setOnClickListener(this)
    }

    private fun showBrushDialog() {
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
        when (view?.id) {
            R.id.btn_brush ->{ showBrushDialog()
            }
            R.id.btn_red -> { drawingView.setBrushColor(Color.RED)
            }
            R.id.btn_green -> { drawingView.setBrushColor(Color.GREEN)
            }
            R.id.btn_blue -> { drawingView.setBrushColor(Color.BLUE)
            }
            R.id.btn_black -> { drawingView.setBrushColor(Color.BLACK)
            }
            R.id.btn_undo -> { drawingView.undoPath()
            }
            R.id.btn_color_pick -> { showColorPicker()
            }
            R.id.btn_gallery -> { requestStoragePermission()
            }
            R.id.btn_save -> {
                Toast.makeText(this, "Save drawing", Toast.LENGTH_SHORT).show()
                val canvas: Bitmap = getCanvasBitMap(drawingView)
                saveDrawing(canvas)

            }
        }
    }

    // color picker dialog
    private fun showColorPicker() {
        val dialog = AmbilWarnaDialog(this, Color.BLUE, object : OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {

            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingView.setBrushColor(color)
            }
        })
        dialog.show()
    }

    // request storage permission
    private fun requestStoragePermission() {
        val permission = Manifest.permission.READ_MEDIA_IMAGES

        when {
            ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                showGallery()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                // Optional: Explain to the user why this permission is needed (you can just request directly too)
                AlertDialog.Builder(this).setTitle("Storage permission")
                    .setMessage("Need permission to access photos")
                    .setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                        requestPermissions.launch(arrayOf(permission))
                    }.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.create().show()
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

    // save drawing canvas
    private fun getCanvasBitMap(view: View): Bitmap{
        val bitmap = createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    private fun saveDrawing(bitmap: Bitmap) {
        val filename = "drawing-${UUID.randomUUID()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "MyDrawings") // custom dict
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = contentResolver
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri == null) {
                throw IOException("Failed to create new MediaStore record.")
            }

            fos = resolver.openOutputStream(imageUri)

            if (fos != null){
                val format = if (filename.endsWith(".png")) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                bitmap.compress(format, 90, fos) // 90 for JPEG quality, 100 for PNG
                fos.flush()
                fos.close()
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0) // Mark the file as complete
                }
                resolver.update(imageUri, contentValues, null, null)
                Toast.makeText(this, "Drawing saved to gallery!", Toast.LENGTH_SHORT).show()
                Log.d("SaveDrawing", "Saved to URI: $imageUri")
            } else {
                Toast.makeText(this, "Failed to get output stream.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(this, "Error saving drawing: ${e.message}", Toast.LENGTH_LONG).show()
            // If saving failed on Android 10+ and IS_PENDING was set, delete the partial file
            imageUri?.let {
                contentResolver.delete(it, null, null)
            }
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}