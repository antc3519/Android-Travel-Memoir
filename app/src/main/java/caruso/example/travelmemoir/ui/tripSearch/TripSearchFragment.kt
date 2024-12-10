package caruso.example.travelmemoir.ui.tripSearch

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import caruso.example.travelmemoir.R
import caruso.example.travelmemoir.ui.friends.FriendsAdapter
import caruso.example.travelmemoir.ui.friends.listedUser
import caruso.example.travelmemoir.ui.tripCreate.tripEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TripSearchFragment(tripEntryId: String) : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripAdapter
    private lateinit var tripList: MutableList<tripEntry>
    private lateinit var filteredList: MutableList<tripEntry>
    private lateinit var searchView: SearchView
    private val db = FirebaseFirestore.getInstance()
    private var tripId: String = tripEntryId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_trip_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.tripListRecycler)
        searchView = view.findViewById(R.id.searchView)
        tripList = mutableListOf()
        filteredList = mutableListOf()

        setupRecyclerView()
        fetchTripEntries()
        setupSearchView()
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(filteredList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun fetchTripEntries() {
        db.collection("tripEntries").get()
            .addOnSuccessListener { documents ->
                tripList.clear()
                for (document in documents) {
                    var photos = document.get("mediaList").toString()
                    var photoArray = photos.split(",").toList()
                    val trip = tripEntry(
                        uid = document.get("uid").toString(),
                        lat = document.get("lat").toString().toDouble(),
                        long = document.get("long").toString().toDouble(),
                        title = document.get("title").toString(),
                        description = document.get("description").toString(),
                        date = document.get("date").toString(),
                        mediaList = photoArray,
                    )
                    if(tripId == "")
                        tripList.add(trip)
                    else if(tripId == document.id){
                        tripList.add(trip)
                    }

                }
                filteredList.addAll(tripList)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        tripId = ""
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterTrips(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTrips(newText)
                return true
            }
        })
    }

    private fun filterTrips(query: String?) {
        filteredList.clear()
        if (!query.isNullOrEmpty()) {
            val lowerCaseQuery = query.lowercase()
            filteredList.addAll(
                tripList.filter { trip ->
                    trip.title.lowercase().contains(lowerCaseQuery) ||
                            trip.description.lowercase().contains(lowerCaseQuery)
                }
            )
        } else {
            filteredList.addAll(tripList)
        }
        adapter.notifyDataSetChanged()
    }
}