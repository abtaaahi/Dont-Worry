package com.abtahiapp.dontworry.activity

import android.app.Dialog
import android.content.pm.PackageManager
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
import android.net.Uri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Environment
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PersonalSpaceActivity : AppCompatActivity() {

    private var isVoiceRecorded = false
    val userId = intent?.getStringExtra("userId") ?: ""
    private var recordedFilePath: String? = null
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
        val btnSubmit = dialog.findViewById<Button>(R.id.btn_submit)
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

        btnSubmit.setOnClickListener {
            if (isVoiceRecorded) {
                val userText = etText.text.toString()
                submitData(userId!!, userText, recordedFilePath)
                Toast.makeText(this, "Submitted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                currentDialog = null
            } else {
                Toast.makeText(this, "Please add a voice message", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateSubmitButton(dialog: Dialog) {
        val btnSubmit = dialog.findViewById<Button>(R.id.btn_submit)
        btnSubmit.isEnabled = isVoiceRecorded
    }

    private fun submitData(userId: String, text: String, voicePath: String?) {
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("user_information")
            .child(userId)
            .child("personal_space")

        val timestamp = System.currentTimeMillis()
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        val storageRef = FirebaseStorage.getInstance().reference.child("personal_space/$userId/$timestamp")

        val dataMap = mutableMapOf<String, Any>()
        dataMap["text"] = text
        dataMap["timestamp"] = date

        val uploadTasks = mutableListOf<Task<Uri>>()

        // Upload voice file if exists
        if (voicePath != null) {
            val voiceFile = Uri.fromFile(File(voicePath))
            val voiceRef = storageRef.child("voiceRecording.mp3")
            val voiceUploadTask = voiceRef.putFile(voiceFile).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Unknown voice upload error")
                }
                voiceRef.downloadUrl
            }
            voiceUploadTask.addOnSuccessListener { uri ->
                dataMap["voiceUrl"] = uri.toString()
            }.addOnFailureListener { exception ->
                val errorMsg = "Failed to upload voice recording: ${exception.message}"
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                Log.e("SubmitData", errorMsg, exception)
            }
            uploadTasks.add(voiceUploadTask) // Add voice upload task to the list
        }

        if (uploadTasks.isEmpty()) {
            Toast.makeText(this, "Nothing to upload", Toast.LENGTH_SHORT).show()
            Log.w("SubmitData", "No voice file provided for upload.")
            return
        }

        Tasks.whenAllComplete(uploadTasks)
            .addOnSuccessListener {
                databaseRef.child(timestamp.toString()).setValue(dataMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
                        Log.i("SubmitData", "Data submitted successfully")
                    }
                    .addOnFailureListener { exception ->
                        val errorMsg = "Failed to save data: ${exception.message}"
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                        Log.e("SubmitData", errorMsg, exception)
                    }
            }
            .addOnFailureListener { exception ->
                val errorMsg = "Failed to upload one or more files: ${exception.message}"
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                Log.e("SubmitData", errorMsg, exception)
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecording()
            } else {
                Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}