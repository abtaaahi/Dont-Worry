package com.abtahiapp.dontworry.activity

import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.abtahiapp.dontworry.R
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.media.MediaRecorder
import android.net.Uri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.adapter.PersonalSpaceAdapter
import com.abtahiapp.dontworry.utils.PersonalItem
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PersonalSpaceActivity : AppCompatActivity() {

    private var isVoiceRecorded = false
    private val userId = intent?.getStringExtra("userId") ?: ""
    private var recordedFilePath: String? = null
    private var currentDialog: Dialog? = null
    private var mediaRecorder: MediaRecorder? = null
    private val AUDIO_PERMISSION_REQUEST_CODE = 101
    private var recordingStartTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeRunnable: Runnable? = null

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

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val personalItems = mutableListOf<PersonalItem>()
        val adapter = PersonalSpaceAdapter(personalItems)
        recyclerView.adapter = adapter

        val userId = intent?.getStringExtra("userId") ?: ""
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("user_information")
            .child(userId)
            .child("personal_space")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                personalItems.clear()
                for (dataSnapshot in snapshot.children) {
                    val text = dataSnapshot.child("text").getValue(String::class.java) ?: ""
                    val timestamp = dataSnapshot.child("timestamp").getValue(String::class.java) ?: ""
                    val voiceUrl = dataSnapshot.child("voiceUrl").getValue(String::class.java) ?: ""
                    personalItems.add(PersonalItem(text, timestamp, voiceUrl))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PersonalSpaceActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })

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

        btnStopRecording.isEnabled = false
        btnSubmit.isEnabled = false

        etText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSubmitButton(dialog)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnStartRecording.setOnClickListener {
            recordedFilePath = startVoiceRecording()
            recordingStartTime = System.currentTimeMillis()
            startUpdatingRecordingTime(tvRecordingStatus)
            btnStopRecording.isEnabled = true
            btnStartRecording.isEnabled = false
            isVoiceRecorded = true
            updateSubmitButton(dialog)
        }

        btnStopRecording.setOnClickListener {
            stopVoiceRecording()
            stopUpdatingRecordingTime()
            btnStartRecording.isEnabled = true
            btnStopRecording.isEnabled = false
            tvRecordingStatus.text = "Recording completed."
            updateSubmitButton(dialog)
        }

        btnSubmit.setOnClickListener {
            if (isVoiceRecorded && etText.text.isNotEmpty()) {
                val userText = etText.text.toString()
                submitData(userId!!, userText, recordedFilePath)
                Toast.makeText(this, "Submitted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                currentDialog = null
            } else {
                Toast.makeText(this, "Please add a voice message and text", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun startUpdatingRecordingTime(tvRecordingStatus: TextView) {
        updateTimeRunnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - recordingStartTime
                val hours = (elapsedMillis / (1000 * 60 * 60)) % 24
                val minutes = (elapsedMillis / (1000 * 60)) % 60
                val seconds = (elapsedMillis / 1000) % 60

                val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                tvRecordingStatus.text = "Recording in progress... $timeFormatted"
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateTimeRunnable!!)
    }

    private fun stopUpdatingRecordingTime() {
        updateTimeRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun updateSubmitButton(dialog: Dialog) {
        val etText = dialog.findViewById<EditText>(R.id.et_text)
        val btnSubmit = dialog.findViewById<Button>(R.id.btn_submit)

        btnSubmit.isEnabled = isVoiceRecorded && etText.text.isNotEmpty()
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
            uploadTasks.add(voiceUploadTask)
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