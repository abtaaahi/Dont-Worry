package com.abtahiapp.dontworry.activity

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.abtahiapp.dontworry.R
import android.media.MediaRecorder
import android.net.Uri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.abtahiapp.dontworry.adapter.PersonalSpaceAdapter
import com.abtahiapp.dontworry.room.PersonalItemDao
import com.abtahiapp.dontworry.room.PersonalItemEntity
import com.abtahiapp.dontworry.room.PersonalSpaceDatabase
import com.abtahiapp.dontworry.utils.NetworkUtil
import com.abtahiapp.dontworry.utils.PersonalItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PersonalSpaceActivity : AppCompatActivity() {

    private var isVoiceRecorded = false
    private var recordedFilePath: String? = null
    private var recordingStartTime: Long = 0
    private var mediaRecorder: MediaRecorder? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeRunnable: Runnable? = null
    private lateinit var db: PersonalSpaceDatabase
    private lateinit var personalItemDao: PersonalItemDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_space)

        db = Room.databaseBuilder(
            applicationContext,
            PersonalSpaceDatabase::class.java, "personal_space_db"
        ).build()

        personalItemDao = db.personalItemDao()
        Log.d("PersonalSpaceActivity", "Database initialized: $db")

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val personalItems = mutableListOf<PersonalItem>()
        val adapter = PersonalSpaceAdapter(personalItems)
        recyclerView.adapter = adapter

        val userId = intent?.getStringExtra("userId") ?: ""

        if (NetworkUtil.isOnline(this)) {
            Log.d("PersonalSpaceActivity", "Online, fetching data from Firebase")
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
                    Log.d("PersonalSpaceActivity", "Fetched ${personalItems.size} items from Firebase")

                    CoroutineScope(Dispatchers.IO).launch {
                        personalItemDao.insertAll(personalItems.map {
                            PersonalItemEntity(it.text, it.timestamp, it.voiceUrl)
                        })
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PersonalSpaceActivity", "Failed to load data: ${error.message}")
                    Toast.makeText(this@PersonalSpaceActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val offlineItems = personalItemDao.getAllItems()
                Log.d("PersonalSpaceActivity", "Fetched ${offlineItems.size} offline items from Room")
                val convertedItems = offlineItems.map {
                    PersonalItem(it.text, it.timestamp, it.voiceUrl.toString())
                }

                runOnUiThread {
                    personalItems.clear()
                    personalItems.addAll(convertedItems)
                    Log.d("PersonalSpaceActivity", "Offline items added to list: ${personalItems.size}")
                    adapter.notifyDataSetChanged()
                }
            }
        }

        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        if (!NetworkUtil.isOnline(this)) {
            fabAdd.isEnabled = false
            fabAdd.alpha = 0.5f
        } else {
            fabAdd.isEnabled = true
            fabAdd.alpha = 1.0f
        }
        fabAdd.setOnClickListener {
            openDialog(userId)
        }
    }

    private fun openDialog(userId: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.personal_space_dialog)
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        val closeButton = dialog.findViewById<ImageButton>(R.id.btn_close_dialog)
        val etText = dialog.findViewById<EditText>(R.id.et_text)
        val btnSubmit = dialog.findViewById<Button>(R.id.btn_submit)
        val tvRecordingStatus = dialog.findViewById<TextView>(R.id.tv_recording_status)

        btnSubmit.visibility = View.GONE

        recordedFilePath = startVoiceRecording()
        recordingStartTime = System.currentTimeMillis()
        startUpdatingRecordingTime(tvRecordingStatus)
        isVoiceRecorded = true

        etText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etText.text.isNotEmpty()) {
                    btnSubmit.visibility = View.VISIBLE
                } else {
                    btnSubmit.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            if (isVoiceRecorded) {
                val userText = etText.text.toString().ifEmpty { "Voice Space" }
                stopVoiceRecording()
                stopUpdatingRecordingTime()
                submitData(userId, userText, recordedFilePath)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please record a voice message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startVoiceRecording(): String? {
        val outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
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
        return recordedFilePath
    }

    private fun stopVoiceRecording() {
        mediaRecorder?.apply {
            try {
                stop()
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

    private fun submitData(userId: String, text: String, voicePath: String?) {
        val timestamp = System.currentTimeMillis()
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        val dataMap = mutableMapOf<String, Any>()
        dataMap["text"] = text
        dataMap["timestamp"] = date

        if (voicePath != null) {
            if (NetworkUtil.isOnline(this)) {
                // Save to Firebase if online
                val storageRef = FirebaseStorage.getInstance().reference.child("personal_space/$userId/$timestamp")
                val voiceFile = Uri.fromFile(File(voicePath))
                val voiceRef = storageRef.child("voiceRecording.mp3")
                voiceRef.putFile(voiceFile).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception ?: Exception("Unknown voice upload error")
                    }
                    voiceRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    dataMap["voiceUrl"] = uri.toString()
                    FirebaseDatabase.getInstance().getReference("user_information")
                        .child(userId)
                        .child("personal_space")
                        .child(timestamp.toString())
                        .setValue(dataMap)
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.IO).launch {
                                personalItemDao.insertItem(PersonalItemEntity(text, date, voicePath))
                                Log.d("PersonalSpaceActivity", "Inserted item to Room: $text")
                            }
                            Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to upload voice", Toast.LENGTH_SHORT).show()
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    personalItemDao.insertItem(PersonalItemEntity(text, date, voicePath))
                    Log.d("PersonalSpaceActivity", "Inserted offline item: $text, date: $date, voicePath: $voicePath")
                    runOnUiThread {
                        Toast.makeText(this@PersonalSpaceActivity, "Data saved offline", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // Text-only entry
            if (NetworkUtil.isOnline(this)) {
                FirebaseDatabase.getInstance().getReference("user_information")
                    .child(userId)
                    .child("personal_space")
                    .child(timestamp.toString())
                    .setValue(dataMap)
                    .addOnSuccessListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            personalItemDao.insertItem(PersonalItemEntity(text, date, null))
                            Log.d("PersonalSpaceActivity", "Inserted text-only item to Room: $text")
                        }
                        Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
                    }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    personalItemDao.insertItem(PersonalItemEntity(text, date, null))
                    runOnUiThread {
                        Toast.makeText(this@PersonalSpaceActivity, "Data saved offline", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}