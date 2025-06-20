package com.project.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
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
import androidx.activity.result.PickVisualMediaRequest
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
            val grantedImages = permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            val grantedUserSelectedImages =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    permissions[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
                } else {
                    false
                }
            when {
                grantedImages -> {
                    Toast.makeText(this, "Full gallery access granted", Toast.LENGTH_SHORT).show()
                }

                grantedUserSelectedImages -> {
                    Toast.makeText(this, "Access to selected photos granted", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(this, "No gallery access granted", Toast.LENGTH_SHORT).show()
                }
            }

        }


    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                findViewById<ImageView>(R.id.gallery_img).setImageURI(uri)
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
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
            R.id.btn_brush -> {
                showBrushDialog()
            }

            R.id.btn_red -> {
                drawingView.setBrushColor(Color.RED)
            }

            R.id.btn_green -> {
                drawingView.setBrushColor(Color.GREEN)
            }

            R.id.btn_blue -> {
                drawingView.setBrushColor(Color.BLUE)
            }

            R.id.btn_black -> {
                drawingView.setBrushColor(Color.BLACK)
            }

            R.id.btn_undo -> {
                drawingView.undoPath()
            }

            R.id.btn_color_pick -> {
                showColorPicker()
            }

            R.id.btn_gallery -> {
                requestStoragePermission()
            }

            R.id.btn_save -> {
                Toast.makeText(this, "Saving drawing", Toast.LENGTH_SHORT).show()
                val drawingCanvas = findViewById<View>(R.id.drawing_canvas)
                val canvas: Bitmap = getCanvasBitMap(drawingCanvas)
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
        val permissionsToRequest = mutableListOf(Manifest.permission.READ_MEDIA_IMAGES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        }

        val fullAccessGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        val partialAccessGranted =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED

        when {
            fullAccessGranted -> showGallery()
            partialAccessGranted -> showGallery()

            shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) -> {
                AlertDialog.Builder(this).setTitle("Permission Required")
                    .setMessage("This app needs access to your photos to work properly.")
                    .setPositiveButton("Allow") { dialog, _ ->
                        dialog.dismiss()
                        requestPermissions.launch(permissionsToRequest.toTypedArray())
                    }.setNegativeButton("Deny") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(this, "Photo access denied.", Toast.LENGTH_SHORT).show()
                    }.show()
            }

            else -> requestPermissions.launch(permissionsToRequest.toTypedArray())
        }
    }


    private fun showGallery() {
        pickPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    // save drawing canvas
    private fun getCanvasBitMap(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    private fun saveDrawing(bitmap: Bitmap) {
        val filename = "drawing-${UUID.randomUUID()}.png"
        val resolver = contentResolver
        var imageUri: Uri? = null

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}${File.separator}MyDrawings"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        try {
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to create new MediaStore record.")

            resolver.openOutputStream(imageUri)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    throw IOException("Failed to save bitmap.")
                }
            }

            // Mark the file as complete
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)

            Toast.makeText(this, "Drawing saved to gallery!", Toast.LENGTH_SHORT).show()
            Log.d("SaveDrawing", "Saved to URI: $imageUri")
        } catch (e: Exception) {
            Log.e("SaveDrawing", "Error saving drawing", e)
            Toast.makeText(this, "Error saving drawing: ${e.message}", Toast.LENGTH_LONG).show()
            imageUri?.let { resolver.delete(it, null, null) }
        }
    }


}