package caruso.example.travelmemoir.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import caruso.example.travelmemoir.LoginActivity
import caruso.example.travelmemoir.R
import caruso.example.travelmemoir.ui.friends.listedUser
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class SettingsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    lateinit var signOutButton: Button
    lateinit var profileUpdateButton: Button
    lateinit var ageInput: SeekBar
    lateinit var ageText: TextView
    lateinit var countryInput: TextInputEditText
    lateinit var languageInput: TextInputEditText
    lateinit var profilePhoto: TextInputEditText
    lateinit var nameInput: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        signOutButton = view.findViewById(R.id.signOutButton)
        profileUpdateButton = view.findViewById(R.id.createTripButton)
        ageInput = view.findViewById(R.id.ageInput)
        ageText = view.findViewById(R.id.photosText)
        countryInput = view.findViewById(R.id.longitude)
        languageInput = view.findViewById(R.id.title)
        profilePhoto = view.findViewById(R.id.description)
        nameInput = view.findViewById(R.id.latitude)

        signOutButton.setOnClickListener{
            signOutUser()
        }

        profileUpdateButton.setOnClickListener{
            updateProfile()
        }

        ageInput.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Update the TextView with SeekBar progress
                ageText.text = "Age: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        getProfile()

        return view
    }

    private fun signOutUser() {
        // Firebase sign-out logic
        FirebaseAuth.getInstance().signOut()

        // Redirect the user to the LoginActivity after sign-out
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish() // Close the current activity (MainActivity)
    }

    private fun getProfile() {


        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is authenticated, proceed with Firestore queries
            val userId = currentUser.uid
            val userListRef = db.collection("userList").whereEqualTo("uid", userId)

            userListRef.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.toObject<listedUser>()

                    ageText.text = "Age: " + user.age
                    ageInput.progress = user.age
                    countryInput.setText(user.country)
                    languageInput.setText(user.language)
                    nameInput.setText(user.name)
                    profilePhoto.setText(user.photo)
                }
                Log.e("Firestore", "User list: Completed")
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error getting user list: ${e.message}")
            }

        }
    }

    private fun updateProfile() {
        val user = FirebaseAuth.getInstance().currentUser

        val firestore = FirebaseFirestore.getInstance()
        val userListRef = user?.let { firestore.collection("userList").document(it.uid) }

        if (userListRef != null) {
            userListRef.get().addOnSuccessListener { document ->
                // Create an empty friendList document
                val userList = mapOf(
                    "uid" to user.uid,
                    "age" to ageInput.progress.toInt(),
                    "country" to countryInput.text.toString(),
                    "language" to languageInput.text.toString(),
                    "name" to nameInput.text.toString(),
                    "photo" to profilePhoto.text.toString()
                )
                userListRef.set(userList).addOnSuccessListener {
                    Log.d("Firestore", "FriendList initialized for user ${user.uid}")
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Error initializing FriendList: ${e.message}")
                }
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error checking FriendList: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}