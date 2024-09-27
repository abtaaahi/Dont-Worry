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
