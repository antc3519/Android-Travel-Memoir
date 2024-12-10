package caruso.example.travelmemoir.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import caruso.example.travelmemoir.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class FriendsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userList: MutableList<listedUser>
    private lateinit var friendUidList: MutableList<String>
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var friendDocID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userList = mutableListOf()
        friendUidList = mutableListOf()

        var recyclerView = view.findViewById<RecyclerView>(R.id.tripListRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter
        friendsAdapter = FriendsAdapter(userList) { user ->
            updateFriends(user)
        }
        recyclerView.adapter = friendsAdapter

        // Fetch the list of users from Firestore
        fetchFriendsList()
    }

    private fun fetchFriendsList() {
        // Get the current user ID
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is authenticated, proceed with Firestore queries
            val userId = currentUser.uid
            val friendListRef = db.collection("friendList").whereEqualTo("uid", userId)

            friendListRef.get().addOnSuccessListener { documents ->
                friendUidList = (documents.first().get("friends") as? MutableList<String>)!!
                Log.e("Firestore", "Friends list: $friendUidList")
                friendDocID = documents.first().id
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error getting friend list: ${e.message}")
            }

            val userListRef = db.collection("userList").whereNotEqualTo("uid", userId)

            userListRef.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.toObject<listedUser>()
                    if (friendUidList.contains(user.uid))
                        user.isFriend = true
                    else user.isFriend = false

                    userList.add(user)
                    // Set the data to the adapter
                    friendsAdapter.notifyDataSetChanged()
                }
                Log.e("Firestore", "User list: ${userList[0].photo}")
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error getting user list: ${e.message}")
            }
        } else {
            Log.e("Firestore", "User is not authenticated")
        }


    }

    private fun updateFriends(user: listedUser) {
        if (user.isFriend)
            friendUidList.add(user.uid)
        else
            friendUidList.remove(user.uid)

        val currentUser = auth.currentUser
        val userDocRef = db.collection("friendList").document(friendDocID)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val friends = document.get("friends") as? MutableList<String>
                if (friends?.contains(user.uid) == true) {
                    // Remove friend if already added
                    friends.remove(user.uid)
                } else {
                    if (friends != null) {
                        friends.add(user.uid)
                    }
                }

                // Update the document
                userDocRef.update("friends", friends).addOnSuccessListener {
                    Toast.makeText(requireContext(), "${user.name} updated in friends list!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error updating friends list: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Error fetching friend list: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}