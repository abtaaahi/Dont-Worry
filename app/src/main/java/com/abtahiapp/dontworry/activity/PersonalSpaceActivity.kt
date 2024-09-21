package com.abtahiapp.dontworry.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.abtahiapp.dontworry.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PersonalSpaceActivity : AppCompatActivity() {

    private var isVoiceRecorded = false
    private var isImageAdded = false

    // To handle gallery image selection
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val dialog = getCurrentDialog() // Get the dialog instance, assume a function exists to manage dialogs
            val ivSelectedImage = dialog.findViewById<ImageView>(R.id.iv_selected_image)
            ivSelectedImage.setImageURI(uri)
            ivSelectedImage.visibility = ImageView.VISIBLE
            isImageAdded = true
            updateSubmitButton(dialog)
        }
    }

    // To handle camera image capture
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val dialog = getCurrentDialog()
            val ivSelectedImage = dialog.findViewById<ImageView>(R.id.iv_selected_image)
            ivSelectedImage.setImageBitmap(bitmap)
            ivSelectedImage.visibility = ImageView.VISIBLE
            isImageAdded = true
            updateSubmitButton(dialog)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_space)

        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        fabAdd.setOnClickListener {
            openDialog()
        }
    }

    private fun openDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.personal_space_dialog)
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()

        val etText = dialog.findViewById<EditText>(R.id.et_text)
        val btnStartRecording = dialog.findViewById<Button>(R.id.btn_start_recording)
        val btnStopRecording = dialog.findViewById<Button>(R.id.btn_stop_recording)
        val btnCamera = dialog.findViewById<Button>(R.id.btn_camera)
        val btnGallery = dialog.findViewById<Button>(R.id.btn_gallery)
        val btnSubmit = dialog.findViewById<Button>(R.id.btn_submit)
        val ivSelectedImage = dialog.findViewById<ImageView>(R.id.iv_selected_image)
        val tvRecordingStatus = dialog.findViewById<TextView>(R.id.tv_recording_status)

        btnStartRecording.setOnClickListener {
            // Start recording logic
            isVoiceRecorded = true
            btnStopRecording.isEnabled = true
            btnStartRecording.isEnabled = false
            tvRecordingStatus.text = "Recording in progress..."
            updateSubmitButton(dialog)
        }

        btnStopRecording.setOnClickListener {
            // Stop recording logic
            isVoiceRecorded = true
            btnStartRecording.isEnabled = true
            btnStopRecording.isEnabled = false
            tvRecordingStatus.text = "Recording completed."
            updateSubmitButton(dialog)
        }

        btnCamera.setOnClickListener {
            // Launch camera intent
            cameraLauncher.launch(null)
        }

        btnGallery.setOnClickListener {
            // Open gallery to choose an image
            galleryLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            if (isVoiceRecorded || isImageAdded) {
                // Submit form logic
                Toast.makeText(this, "Submitted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please add a voice message or an image", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateSubmitButton(dialog: Dialog) {
        val btnSubmit = dialog.findViewById<Button>(R.id.btn_submit)
        btnSubmit.isEnabled = isVoiceRecorded || isImageAdded
    }

    // Helper function to get the current dialog instance (you can maintain it using a variable or adjust this as needed)
    private fun getCurrentDialog(): Dialog {
        // You can manage the dialog as needed, for simplicity assume it's directly accessible
        // This is just a placeholder to get the current dialog instance
        return Dialog(this)
    }
}
