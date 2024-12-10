package caruso.example.travelmemoir

import android.app.Application
import com.google.firebase.FirebaseApp

class Firebase : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}