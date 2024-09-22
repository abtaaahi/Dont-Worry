package com.abtahiapp.dontworry.activity

import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.abtahiapp.dontworry.R
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.media.MediaRecorder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Environment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PersonalSpaceActivity : AppCompatActivity() {

    private var isVoiceRecorded = false
    private var isImageAdded = false
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    val userId = intent?.getStringExtra("userId") ?: ""
    private var recordedFilePath: String? = null
    private var imageUri: Uri? = null
    private var currentDialog: Dialog? = null
    private var mediaRecorder: MediaRecorder? = null
    private val AUDIO_PERMISSION_REQUEST_CODE = 101

    private fun startVoiceRecording(): String {
        if (checkAudioPermission()) {
            val outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC) // Use app's private storage
            val outputFile = File(outputDir, "voice_${System.currentTimeMillis()}.mp3")
            recordedFilePath = outputFile.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recordedFilePath)

                try {
                    prepare()
                    start()
                    Toast.makeText(this@PersonalSpaceActivity, "Recording started", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@PersonalSpaceActivity, "Recording failed", Toast.LENGTH_SHORT).show()
                }
            }

            return recordedFilePath ?: ""
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                AUDIO_PERMISSION_REQUEST_CODE
            )
            return ""
        }
    }

    private fun stopVoiceRecording() {
        mediaRecorder?.apply {
            try {
                stop() // Stop recording
                reset()
                release()
                Toast.makeText(this@PersonalSpaceActivity, "Recording stopped", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PersonalSpaceActivity, "Failed to stop recording", Toast.LENGTH_SHORT).show()
            }
        }
        mediaRecorder = null
    }

    private fun checkAudioPermission(): Boolean {
        val recordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        return recordPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val dialog = currentDialog // Get the current opened dialog
            dialog?.let {
                val ivSelectedImage = it.findViewById<ImageView>(R.id.iv_selected_image)
                ivSelectedImage.setImageURI(uri)
                ivSelectedImage.visibility = ImageView.VISIBLE
                isImageAdded = true
                updateSubmitButton(it)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val dialog = currentDialog
            dialog?.let {
                val ivSelectedImage = it.findViewById<ImageView>(R.id.iv_selected_image)
                ivSelectedImage.setImageBitmap(bitmap)
                ivSelectedImage.visibility = ImageView.VISIBLE
                imageUri = saveBitmapToFile(bitmap)
                isImageAdded = true
                updateSubmitButton(it)
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        val tempFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_image.jpg")
        tempFile.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return Uri.fromFile(tempFile)
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

        currentDialog = dialog

        val etText = dialog.findViewById<EditText>(R.id.et_text)
        val btnStartRecording = dialog.findViewById<Button>(R.id.btn_start_recording)
        val btnStopRecording = dialog.findViewById<Button>(R.id.btn_stop_recording)
        val btnCamera = dialog.findViewById<Button>(R.id.btn_camera)
        val btnGallery = dialog.findViewById<Button>(R.id.btn_gallery)
        val btnSubmit = dialog.findViewById<Button>(R.id.btn_submit)
        val ivSelectedImage = dialog.findViewById<ImageView>(R.id.iv_selected_image)
        val tvRecordingStatus = dialog.findViewById<TextView>(R.id.tv_recording_status)

        btnStartRecording.setOnClickListener {
            recordedFilePath = startVoiceRecording()
            btnStopRecording.isEnabled = true
            btnStartRecording.isEnabled = false
            tvRecordingStatus.text = "Recording in progress..."
            isVoiceRecorded = true
            updateSubmitButton(dialog)
        }

        btnStopRecording.setOnClickListener {
            stopVoiceRecording()
            btnStartRecording.isEnabled = true
            btnStopRecording.isEnabled = false
            tvRecordingStatus.text = "Recording completed."
            updateSubmitButton(dialog)
        }

        btnCamera.setOnClickListener {
            if (checkCameraPermission()){
                cameraLauncher.launch(null)
            }
        }

        btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            if (isVoiceRecorded || isImageAdded) {
                val userText = etText.text.toString()
                submitData(userId!!, userText, recordedFilePath, imageUri)
                Toast.makeText(this, "Submitted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                currentDialog = null
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

    private fun submitData(userId: String, text: String, voicePath: String?, imageUri: Uri?) {
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("user_information/$userId/personal_space")

        val timestamp = System.currentTimeMillis()
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        val storageRef = FirebaseStorage.getInstance().reference.child("personal_space/$userId/$timestamp")

        val dataMap = mutableMapOf<String, Any>()
        dataMap["text"] = text
        dataMap["timestamp"] = date

        var isUploadSuccessful = false

        var isDataSaved = false

        if (voicePath != null) {
            val voiceFile = Uri.fromFile(File(voicePath))
            val voiceRef = storageRef.child("voiceRecording.mp3")
            voiceRef.putFile(voiceFile)
                .addOnSuccessListener {
                    voiceRef.downloadUrl.addOnSuccessListener { uri ->
                        dataMap["voiceUrl"] = uri.toString()
                        isUploadSuccessful = true

                        if (!isDataSaved || imageUri == null) {
                            databaseRef.child(timestamp.toString()).setValue(dataMap)
                            isDataSaved = true
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload voice recording", Toast.LENGTH_SHORT).show()
                }
        }

        if (imageUri != null) {
            val imageRef = storageRef.child("image.jpg")
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        dataMap["imageUrl"] = uri.toString()
                        isUploadSuccessful = true

                        if (!isDataSaved || voicePath == null) {
                            databaseRef.child(timestamp.toString()).setValue(dataMap)
                            isDataSaved = true
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }

        if (voicePath == null && imageUri == null) {
            Toast.makeText(this, "Nothing to upload", Toast.LENGTH_SHORT).show()
            return
        }

        if (isUploadSuccessful) {
            Toast.makeText(this, "Submitted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to upload both voice and image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(null)
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecording()
            } else {
                Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
