package caruso.example.travelmemoir.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import caruso.example.travelmemoir.R
import com.bumptech.glide.Glide

class FriendsAdapter(
    private val userList: List<listedUser>,
    private val FriendButtonClick: (listedUser) -> Unit
): RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>(){

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tripName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tripDescription)
        private val photoImageView: ImageView = itemView.findViewById(R.id.user_image)
        private val friendButton: Button = itemView.findViewById(R.id.update_friendship)

        fun bind(user: listedUser) {
            nameTextView.text = user.name
            descriptionTextView.text = "Age: ${user.age} \nCountry: ${user.country} \nLanguage: ${user.language}"
            Glide.with(itemView)
                .load(user.photo)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .into(photoImageView)
            if (user.isFriend)
                friendButton.text = "Remove Friend"
            else friendButton.text = "Add Friend"

            friendButton.setOnClickListener {
                if (user.isFriend) {
                    user.isFriend = false
                    friendButton.text = "Add Friend"
                    FriendButtonClick(user)
                }
                else {
                    user.isFriend = true
                    friendButton.text = "Remove Friend"
                    FriendButtonClick(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friends_list_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(userList[position])
    }


}