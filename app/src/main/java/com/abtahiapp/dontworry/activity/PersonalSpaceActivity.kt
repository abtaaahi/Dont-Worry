package com.abtahiapp.dontworry.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.abtahiapp.dontworry.R
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.abtahiapp.dontworry.adapter.PersonalSpaceAdapter
import com.abtahiapp.dontworry.apiservice.TextBlobApiService
import com.abtahiapp.dontworry.room.PersonalItemDao
import com.abtahiapp.dontworry.room.PersonalItemEntity
import com.abtahiapp.dontworry.room.PersonalSpaceDatabase
import com.abtahiapp.dontworry.utils.NetworkUtil
import com.abtahiapp.dontworry.utils.PersonalItem
import com.abtahiapp.dontworry.utils.RetrofitClient
import com.abtahiapp.dontworry.utils.SentimentRequest
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import com.abtahiapp.dontworry.utils.InfoBottomSheetDialog

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
    private var selectedPosition: Int = -1
    private var audioPermissionsCallback: ((Boolean) -> Unit)? = null

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

        adapter = PersonalSpaceAdapter(personalItems) { selectedPos ->
            selectedPosition = selectedPos
        }
        recyclerView.adapter = adapter

        val infoButton = findViewById<ImageButton>(R.id.infoButton)
        infoButton.setOnClickListener {

            val infoMessage = getString(R.string.personal_space_info_message).trimIndent()

            val infoBottomSheetDialog = InfoBottomSheetDialog(this, infoMessage)
            infoBottomSheetDialog.show()
        }

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
        val photoUrl = intent?.getStringExtra("photoUrl") ?: ""

        val fabAdd = findViewById<ExtendedFloatingActionButton>(R.id.fab_add)
        val fabAddChat = findViewById<ExtendedFloatingActionButton>(R.id.fab_add_chat)
        if (!NetworkUtil.isOnline(this)) {
            fabAdd.isEnabled = false
            fabAdd.alpha = 0.5f
            fabAddChat.isEnabled = false
            fabAddChat.alpha = 0.5f
        } else {
            fabAdd.isEnabled = true
            fabAdd.alpha = 1.0f
            fabAddChat.isEnabled = true
            fabAddChat.alpha = 1.0f
        }
        fabAdd.setOnClickListener {
            requestAudioPermissions { isGranted ->
                if (isGranted) {
                    openDialog(userId)
                } else {
                    Toast.makeText(this, "Audio permissions are required", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fabAddChat.setOnClickListener {
            val intent = Intent(this, ChatbotActivity::class.java)
            intent.putExtra("userID", userId)
            intent.putExtra("photoUrl", photoUrl)
            startActivity(intent)
        }

        val btnAnalysis = findViewById<Button>(R.id.btn_analysis)
        btnAnalysis.setOnClickListener {
            if (!NetworkUtil.isOnline(this)) {
                Toast.makeText(
                    this,
                    "Connect to the internet to use this feature.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val itemToAnalyze =
                if (selectedPosition != -1) personalItems[selectedPosition] else personalItems[0]

            if (itemToAnalyze.voiceUrl != null) {
                analyzeVoice(itemToAnalyze.voiceUrl!!)
                adapter.selectedPosition = -1
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(
                    this,
                    "No audio file found for the selected item.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun requestAudioPermissions(callback: (Boolean) -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            callback(true)
            return}

        ActivityCompat.requestPermissions(this, permissions, AUDIO_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            val isGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            audioPermissionsCallback?.invoke(isGranted)
            audioPermissionsCallback = null
        }
    }

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 123
    }

    private fun analyzeVoice(voiceUrl: String) {
        val audioFile = File(voiceUrl)

        if (!audioFile.exists()) {
            Toast.makeText(this, "Audio file not found.", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = MultipartBody.Part.createFormData(
            "media", audioFile.name, audioFile.asRequestBody("audio/mp3".toMediaTypeOrNull())
        )

        val dialogView = layoutInflater.inflate(R.layout.dialog_analyze, null)
        val progressBar = dialogView.findViewById<LottieAnimationView>(R.id.progress_bar)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.show()
        tvMessage.text = "Processing the voice..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val transcriptionResponse = RetrofitClient.revInstance.submitTranscriptionJob(requestFile).execute()

                if (transcriptionResponse.isSuccessful) {
                    val jobId = extractJobId(transcriptionResponse.body()?.string())

                    delay(10000)

                    val transcriptResponse = RetrofitClient.revInstance.getTranscriptionResult(jobId).execute()

                    if (transcriptResponse.isSuccessful) {
                        val transcriptJson = transcriptResponse.body()?.string()
                        Log.e("Transcription", "Response: $transcriptJson")

                        if (transcriptJson != null) {
                            val transcribedText = extractTranscribedText(transcriptJson)
                            analyzeSentiment(transcribedText, tvMessage, progressBar)
                        } else {
                            updateUIOnError("No transcription available.", tvMessage, progressBar)
                        }
                    } else {
                        updateUIOnError("Error fetching transcription: ${transcriptResponse.message()}", tvMessage, progressBar)
                    }
                } else {
                    updateUIOnError("Error starting transcription: ${transcriptionResponse.message()}", tvMessage, progressBar)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateUIOnError("Error: ${e.message}", tvMessage, progressBar)
            }
        }
    }

        private fun analyzeSentiment(transcribedText: String, tvMessage: TextView, progressBar: LottieAnimationView) {
            val apiService = RetrofitClient.create(TextBlobApiService::class.java)
            val sentimentRequest = SentimentRequest(transcribedText)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val sentimentResponse = apiService.analyzeSentiment(sentimentRequest).execute()

                    if (sentimentResponse.isSuccessful) {
                        val sentimentResult = sentimentResponse.body()

                        runOnUiThread {
                            tvMessage.text = "Voice Space: ${transcribedText}\nSentiment: ${sentimentResult?.sentiment}\nSentiment Score: ${sentimentResult?.score}"
                            progressBar.visibility = View.GONE
                        }
                    } else {
                        updateUIOnError("Error fetching sentiment: ${sentimentResponse.message()}", tvMessage, progressBar)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    updateUIOnError("Error analyzing sentiment: ${e.message}", tvMessage, progressBar)
                }
            }
        }

        private fun updateUIOnError(errorMessage: String, tvMessage: TextView, progressBar: LottieAnimationView) {
            runOnUiThread {
                tvMessage.text = errorMessage
                progressBar.visibility = View.GONE
            }
        }

        private fun openDialog(userId: String) {
        val dialogView = layoutInflater.inflate(R.layout.personal_space_dialog, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val closeButton = dialogView.findViewById<ImageButton>(R.id.btn_close_dialog)
        val etText = dialogView.findViewById<EditText>(R.id.et_text)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit)
        val tvRecordingStatus = dialogView.findViewById<TextView>(R.id.tv_recording_status)

        btnSubmit.visibility = View.GONE

        recordedFilePath = startVoiceRecording()
        recordingStartTime = System.currentTimeMillis()
        startUpdatingRecordingTime(tvRecordingStatus)
        isVoiceRecorded = true

        etText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnSubmit.visibility = if (etText.text.isNotEmpty()) View.VISIBLE else View.GONE
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
    }

    fun extractJobId(responseBody: String?): String {
        if (responseBody == null || responseBody.isEmpty()) {
            throw IllegalArgumentException("Response body is empty or null")
        }

        val jsonObject = JSONObject(responseBody)
        return jsonObject.getString("id")
    }

    fun extractTranscribedText(responseBody: String): String {
        val jsonObject = JSONObject(responseBody)
        val monologues = jsonObject.getJSONArray("monologues")
        val stringBuilder = StringBuilder()

        for (i in 0 until monologues.length()) {
            val monologue = monologues.getJSONObject(i)
            val elements = monologue.getJSONArray("elements")

            for (j in 0 until elements.length()) {
                val element = elements.getJSONObject(j)
                if (element.getString("type") == "text") {
                    stringBuilder.append(element.getString("value")).append(" ")
                }
            }
        }

        return stringBuilder.toString().trim()
    }
}