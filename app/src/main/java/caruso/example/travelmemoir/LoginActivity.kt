package caruso.example.travelmemoir

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"


    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAuth.getInstance().useAppLanguage()
        FirebaseAuth.getInstance().setLanguageCode("en")
        // Start the sign-in flow when the activity is launched
        startSignInFlow()
    }

    private fun startSignInFlow() {

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is signed in, go directly to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // User is not signed in, show the login UI

            // List of authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),   // Email/Password
            )

            // Launch the FirebaseUI sign-in intent
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false) // Disable SmartLock if you don't want it
                .build()
            signInLauncher.launch(signInIntent)
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {

        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser

            val firestore = FirebaseFirestore.getInstance()
            val friendListRef = user?.let { firestore.collection("friendList").document(it.uid) }

            if (friendListRef != null) {
                friendListRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        // Create an empty friendList document
                        val emptyFriendList = mapOf(
                            "uid" to user.uid,
                            "friends" to emptyList<String>() // Empty map for friends
                        )
                        friendListRef.set(emptyFriendList).addOnSuccessListener {
                            Log.d("Firestore", "FriendList initialized for user ${user.uid}")
                        }.addOnFailureListener { e ->
                            Log.e("Firestore", "Error initializing FriendList: ${e.message}")
                        }
                    } else {
                        Log.d("Firestore", "FriendList already exists for user ${user.uid}")
                    }
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Error checking FriendList: ${e.message}")
                }
            }

            val userListRef = user?.let { firestore.collection("userList").document(it.uid) }

            if (userListRef != null) {
                userListRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        // Create an empty friendList document
                        val emptyUserList = mapOf(
                            "uid" to user.uid,
                            "age" to 0,
                            "country" to "N/A",
                            "language" to "N/A",
                            "name" to "N/A",
                            "photo" to "N/A"
                        )
                        userListRef.set(emptyUserList).addOnSuccessListener {
                            Log.d("Firestore", "FriendList initialized for user ${user.uid}")
                        }.addOnFailureListener { e ->
                            Log.e("Firestore", "Error initializing FriendList: ${e.message}")
                        }
                    } else {
                        Log.d("Firestore", "FriendList already exists for user ${user.uid}")
                    }
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Error checking FriendList: ${e.message}")
                }
            }

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            if (response == null) {
                // User pressed the back button
                finish()
            } else {
                // Handle error (response.error?.errorCode)
                // Example: Toast for error
                toast("Sign-in failed. Please try again.")
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}