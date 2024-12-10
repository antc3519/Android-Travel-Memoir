package caruso.example.travelmemoir.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import caruso.example.travelmemoir.R
import caruso.example.travelmemoir.ui.tripCreate.TripCreateFragment
import caruso.example.travelmemoir.ui.tripSearch.TripSearchFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var mapMarkers: MutableList<marker>
    private lateinit var friendUidList: MutableList<String>
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        mapMarkers = mutableListOf()
        friendUidList = mutableListOf()
        fetchTripEntries()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        // Check for location permissions and request them if not granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else { initializeMap() }
    }

    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun fetchTripEntries() {
        if (currentUser != null) {
            val userId = currentUser.uid

            // Step 1: Get the user's friend list
            db.collection("friendList")
                .whereEqualTo("uid", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val friendList = documents.first().get("friends") as List<String>
                        val uidList = mutableListOf<String>().apply {
                            add(userId) // Add the user's UID
                            addAll(friendList) // Add friend's UIDs
                        }

                        // Step 2: Query tripEntry for each chunk of UIDs
                        val chunkedUidLists = uidList.chunked(10)
                        val results = mutableListOf<DocumentSnapshot>()

                        chunkedUidLists.forEach { chunk ->
                            db.collection("tripEntries")
                                .whereIn("uid", chunk)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    results.addAll(querySnapshot.documents)

                                    // If all chunks have been queried
                                    if (results.size >= uidList.size || results.size >= querySnapshot.size()) {
                                        processTripEntries(results)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error fetching trip entries: ${e.message}")
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching friend list: ${e.message}")
                }
        } else {
            Log.e("Firestore", "User is not authenticated")
        }
    }

    private fun processTripEntries(documents: List<DocumentSnapshot>) {
        for (trip in documents) {
            val mark: marker
            val tripId = trip.id
            if (trip.get("uid") == currentUser?.uid) {
                mark = marker(
                    uid = trip.get("uid").toString(),
                    lat = trip.get("lat").toString().toDouble(),
                    long = trip.get("long").toString().toDouble(),
                    title = trip.get("title").toString(),
                    date = trip.get("date").toString(),
                    type = "user",
                    tripID = tripId
                )
                mapMarkers.add(mark)
            }
            else {
                mark = marker(
                    uid = trip.get("uid").toString(),
                    lat = trip.get("lat").toString().toDouble(),
                    long = trip.get("long").toString().toDouble(),
                    title = trip.get("title").toString(),
                    date = trip.get("date").toString(),
                    type = "friend",
                    tripID = tripId
                )
                mapMarkers.add(mark)
            }
            Log.d("Firestore", "Trip Entry: ${mark.title}")
        }
        for (marker in mapMarkers){
            val position = LatLng(marker.lat, marker.long)
            var color = BitmapDescriptorFactory.HUE_RED
            if (marker.type == "friend")
                color = BitmapDescriptorFactory.HUE_MAGENTA
            Log.e("HOME", "getTrips: ${marker.title}")
            val mark = googleMap.addMarker(

                MarkerOptions()
                    .position(position)
                    .title(marker.title)
                    .snippet(marker.date)
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
            )
            if (mark != null) {
                mark.tag = marker.tripID
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        var latLong = LatLng(0.0,0.0)
        val zoom = 2f
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, zoom))
        googleMap.uiSettings.isZoomControlsEnabled = true

        map.setOnMapLongClickListener { latLng ->
            val latitude = latLng.latitude
            val longitude = latLng.longitude

            // Display the latitude and longitude
            Toast.makeText(
                requireContext(),
                "Clicked Location: \nLat: $latitude, Lng: $longitude",
                Toast.LENGTH_LONG
            ).show()
            fragmentManager?.beginTransaction()?.replace(R.id.frame_layout, TripCreateFragment(latitude,longitude))?.commit()
        }

        var lastClickTime: Long = 0

        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            val currentClickTime = System.currentTimeMillis()
            Log.e("HOME", "MARKER ", )
            if (currentClickTime - lastClickTime < 5000) {  // 300ms for double-click detection
                // Double-click detected, open trip details
                val tripEntryId = marker.tag as String  // Assuming each marker has its trip entry ID as tag
                openTripDetails(tripEntryId)
            }

            lastClickTime = currentClickTime
            true  // Return true to prevent the info window from showing
        }
    }

    fun openTripDetails(tripEntryId: String) {
        fragmentManager?.beginTransaction()?.replace(R.id.frame_layout, TripSearchFragment(tripEntryId))?.commit()
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(callback: (Location) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                callback(it)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap()
            } else {
                // Handle permission denial
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}