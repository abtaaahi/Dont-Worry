# Don't Worry - Mental Health Tracking

Don't Worry is an Android app designed to assist users in tracking and improving their mental health through mindfulness practices and self-awareness tools. This app offers personalized features, including Google Sign-In for user authentication, a main dashboard for daily insights, and smooth animations to enhance the user experience.

## Features

- **Google Sign-In Integration**: Securely log in and access personalized content.
- **Splash Screen**: Enjoy a brief splash screen before entering the app.
- **Smooth Transitions**: Seamless transitions between activities.
- **Main Dashboard**: Access your mental health tracking tools, insights, and more.
- **Sign-In Page**: Easily sign in or create an account if no previous account is detected.

## Code Snippet

In the `SplashActivity`, the app checks if the user is already signed in with Google. Based on the sign-in status, the app either directs the user to the main dashboard or the sign-in page:

```kotlin
class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("account", account)
                startActivity(intent)
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
            } else {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
            }
            finish()
        }, SPLASH_TIME_OUT)
    }
}
```
## Store Mood in Firebase Realtime Database:

```kotlin
val database = FirebaseDatabase.getInstance().getReference("user_information")
val moodHistoryRef = database.child(account.id!!).child("mood_history")

moodHistoryRef.orderByChild("date").equalTo(currentDate)
    .addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (!dataSnapshot.exists()) {
                showMoodDialog(account.id!!)
            } else {
                val lastMood = dataSnapshot.children.last().child("mood").getValue(String::class.java)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Toast.makeText(this@MainActivity, "Failed to check mood history", Toast.LENGTH_SHORT).show()
        }
    })

private fun storeMoodInDatabase(userId: String, moodName: String, details: String) {
        val database = FirebaseDatabase.getInstance().getReference("user_information")

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentDateTime = SimpleDateFormat("hh:mm a dd MMM", Locale.getDefault()).format(Date())

        val moodData = mapOf(
            "date" to currentDate,
            "dateTime" to currentDateTime,
            "mood" to moodName,
            "details" to details
        )

        database.child(userId).child("mood_history").push().setValue(moodData).addOnSuccessListener {
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save mood data", Toast.LENGTH_SHORT).show()
        }
    }
```
## Dependencies

```kts

dependencies {
    implementation (libs.play.services.auth)
    implementation (libs.glide)
    implementation (libs.circleimageview)
    implementation (libs.core)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation (libs.exoplayer)
    implementation(libs.firebase.database)
    implementation(libs.play.services.location)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.fragment.ktx)
    implementation (libs.socket.io.client)
    implementation(libs.lottie)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.swiperefreshlayout)
}

```
## Server Creation

```javascript

const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const { MongoClient } = require('mongodb');
const app = express();
const server = http.createServer(app);
const io = socketIo(server);

const mongoURI = 'mongodb+srv://abtaaahi_dontworry:8d3fmnkz3JuZdii9@cluster0.jg6nk.mongodb.net/abtaaahi_dontworry?retryWrites=true&w=majority';

const client = new MongoClient(mongoURI, { useNewUrlParser: true, useUnifiedTopology: true });

let collection;

async function connectToMongoDB() {
    await client.connect();
    console.log("Connected to MongoDB Atlas!");
    const db = client.db('abtaaahi_dontworry');
    collection = db.collection('user_status');
}

app.use(express.static('app/src/main/javascript/chat-app/public'));

io.on('connection', async (socket) => {
    const userId = socket.handshake.query.userId;

    if (userId) {
        try {
            await collection.updateOne({ user_id: userId }, { $set: { status: 'online' } }, { upsert: true });

            io.emit('user-status-change', { userId, status: 'online' });

            const allStatuses = await collection.find({}).toArray();
            socket.emit('all-user-status', allStatuses);

            console.log(`${userId} connected`);

            socket.on('disconnect', async () => {
                await collection.updateOne({ user_id: userId }, { $set: { status: 'offline' } });

                io.emit('user-status-change', { userId, status: 'offline' });
                console.log(`${userId} disconnected`);
            });
        } catch (error) {
            console.error('Error with database operation', error);
        }
    }
});

const PORT = process.env.PORT || 3000;

connectToMongoDB().then(() => {
    server.listen(PORT, () => {
        console.log(`Server running on port ${PORT}`);
    });
}).catch(err => {
    console.error('Error connecting to MongoDB:', err);
});

```