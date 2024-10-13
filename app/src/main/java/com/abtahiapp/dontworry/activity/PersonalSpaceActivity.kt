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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PersonalSpaceActivity : AppCompatActivity() {

    private var isVoiceRecorded = false
    private var recordedFilePath: String? = null
    private var recordingStartTime: Long = 0
    private var mediaRecorder: MediaRecorder? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeRunnable: Runnable? = null
    private lateinit var db: PersonalSpaceDatabase
    private lateinit var personalItemDao: PersonalItemDao
    private lateinit var adapter: PersonalSpaceAdapter
    private val personalItems = mutableListOf<PersonalItem>()
    private lateinit var username: String
    private lateinit var tfliteInterpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_space)

        tfliteInterpreter = Interpreter(loadModelFile())

        db = Room.databaseBuilder(
            applicationContext,
            PersonalSpaceDatabase::class.java, "personal_space_db"
        ).build()

        personalItemDao = db.personalItemDao()
        Log.d("PersonalSpaceActivity", "Database initialized: $db")

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PersonalSpaceAdapter(personalItems)
        recyclerView.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            val offlineItems = personalItemDao.getAllItems()
            Log.d("PersonalSpaceActivity", "Fetched ${offlineItems.size} offline items from Room")

            if (offlineItems.isEmpty()) {
                Log.d("PersonalSpaceActivity", "No items found in the database.")
            }

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

        val userId = intent?.getStringExtra("userId") ?: ""
        username = intent?.getStringExtra("name") ?: ""

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

        val btnAnalysis = findViewById<Button>(R.id.btn_analysis)
        btnAnalysis.setOnClickListener {
            if (personalItems.isNotEmpty()) {
                val firstAudioPath = personalItems[0].voiceUrl
                analyzeMood(firstAudioPath)
            } else {
                Toast.makeText(this, "No audio recordings available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("yamnet.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        return inputStream.channel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
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

        val filenameDate = SimpleDateFormat("ddMMMyy_hhmma", Locale.getDefault()).format(Date(timestamp)).lowercase()

        val filename = "${username}_${userId}_voice_$filenameDate.mp3"

        if (voicePath != null) {
            CoroutineScope(Dispatchers.IO).launch {
                personalItemDao.insertItem(PersonalItemEntity(text, date, voicePath))
                Log.d("PersonalSpaceActivity", "Inserted offline item: $text, date: $date, voicePath: $voicePath")
                runOnUiThread {
                    //Toast.makeText(this@PersonalSpaceActivity, "Data saved offline", Toast.LENGTH_SHORT).show()
                    refreshRecyclerView(adapter, personalItems)
                }
            }

            val storageRef = FirebaseStorage.getInstance().reference
                .child("personal_space/$filename")

            val voiceFile = Uri.fromFile(File(voicePath))

            storageRef.putFile(voiceFile)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("PersonalSpaceActivity", "Voice uploaded to Firebase: $uri")
                        //Toast.makeText(this@PersonalSpaceActivity, "Voice saved to Firebase", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PersonalSpaceActivity", "Failed to upload voice: ${exception.message}")
                    //Toast.makeText(this@PersonalSpaceActivity, "Failed to upload voice to Firebase", Toast.LENGTH_SHORT).show()
                }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                personalItemDao.insertItem(PersonalItemEntity(text, date, null))
                runOnUiThread {
                    //Toast.makeText(this@PersonalSpaceActivity, "Data saved offline", Toast.LENGTH_SHORT).show()
                    refreshRecyclerView(adapter, personalItems)
                }
            }
        }
    }

    private fun refreshRecyclerView(adapter: PersonalSpaceAdapter, personalItems: MutableList<PersonalItem>) {
        CoroutineScope(Dispatchers.IO).launch {
            val updatedItems = personalItemDao.getAllItems().map {
                PersonalItem(it.text, it.timestamp, it.voiceUrl.toString())
            }

            runOnUiThread {
                personalItems.clear()
                personalItems.addAll(updatedItems)
                adapter.notifyDataSetChanged()
                Log.d("PersonalSpaceActivity", "RecyclerView refreshed with ${personalItems.size} items")
            }
        }
    }

    override fun onBackPressed() {
        adapter.releaseMediaPlayer()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.releaseMediaPlayer()
        tfliteInterpreter.close()
    }

    private fun analyzeMood(audioPath: String) {
        val audioData = loadAudioData(audioPath)

        if (audioData != null) {
            val outputSize= 521
            val output = Array(1) { FloatArray(outputSize) }
            tfliteInterpreter.run(audioData, output)

            val predictedMood = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            val moodLabel = getMoodLabel(predictedMood)
            Toast.makeText(this, "Predicted mood: $moodLabel", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error loading audio data.", Toast.LENGTH_SHORT).show()
        }}

    private fun loadAudioData(audioPath: String): FloatArray? {
        try {
            val requiredSize = 15600
            val audioData = FloatArray(requiredSize)

            val file = File(audioPath)
            val inputStream = FileInputStream(file)
            val buffer = ByteArray(requiredSize * 4)

            var bytesRead = inputStream.read(buffer)
            var index = 0
            while (bytesRead != -1 && index < requiredSize) {
                val floatValue = ByteBuffer.wrap(buffer, index * 4, 4).order(ByteOrder.nativeOrder()).getFloat()
                audioData[index] = floatValue
                index++
                if (index >= requiredSize) {
                    break
                }

                if (bytesRead < requiredSize * 4) {
                    bytesRead = inputStream.read(buffer, bytesRead, (requiredSize * 4) - bytesRead)
                }
            }

            inputStream.close()

            return audioData
        } catch (e: Exception) {
            Log.e("PersonalSpaceActivity", "Error loading audio data: ${e.message}")
            return null
        }
    }

    private fun getMoodLabel(index: Int): String {
        return when (index) {
            0 -> "Happy"
            1 -> "Sad"
            2 -> "Angry"
            3 -> "Neutral"
            4 -> "Fearful"
            5 -> "Disgusted"
            else -> "Unknown Mood"
        }
    }
}