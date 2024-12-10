package caruso.example.travelmemoir.ui.tripCreate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import caruso.example.travelmemoir.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TripCreateFragment(latitude: Double, longitude: Double) : Fragment() {

    private var long: Double = longitude
    private var lat: Double = latitude
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var latInput: TextInputEditText
    private lateinit var longInput: TextInputEditText
    private lateinit var titleInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var dateInput: TextInputEditText
    private lateinit var photoInput: TextInputEditText
    private lateinit var createTripButton: Button
    val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_create, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        latInput = view.findViewById(R.id.latitude)
        longInput = view.findViewById(R.id.longitude)
        titleInput = view.findViewById(R.id.title)
        descriptionInput = view.findViewById(R.id.description)
        dateInput = view.findViewById(R.id.date)
        photoInput = view.findViewById(R.id.photoLinks)
        createTripButton = view.findViewById(R.id.createTripButton)

        createTripButton.setOnClickListener{
            createTrip()
        }

        longInput.setText(long.toString())
        latInput.setText(lat.toString())

        return view
    }

    private fun createTrip(){
        var photos = photoInput.text.toString()
        var photoArray = photos.split(",").toList()
        var trip = currentUser?.let {
            tripEntry(
                uid = it.uid,
                date = dateInput.text.toString(),
                description = descriptionInput.text.toString(),
                long = longInput.text.toString().toDouble(),
                lat = latInput.text.toString().toDouble(),
                title = titleInput.text.toString(),
                mediaList = photoArray
            )
        }

        if (trip != null) {
            db.collection("tripEntries")
                .add(trip) // Automatically generates a unique document ID
                .addOnSuccessListener { documentReference ->
                    Log.d("Firestore", "TripEntry added with ID: ${documentReference.id}")
                    Toast.makeText(context, "TripEntry uploaded successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error adding TripEntry", e)
                    Toast.makeText(context, "Failed to upload TripEntry: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}