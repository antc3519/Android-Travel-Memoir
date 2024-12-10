package caruso.example.travelmemoir.ui.tripSearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import caruso.example.travelmemoir.R
import caruso.example.travelmemoir.ui.tripCreate.tripEntry
import com.bumptech.glide.Glide

class TripAdapter(private val tripList: List<tripEntry>) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tripName)
        private val dateTextView: TextView = itemView.findViewById(R.id.date)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tripDescription)
        private val photoImageView1: ImageView = itemView.findViewById(R.id.user_image)
        private val photoImageView2: ImageView = itemView.findViewById(R.id.user_image2)

        fun bind(trip: tripEntry) {
            titleTextView.text = trip.title
            dateTextView.text = trip.date
            descriptionTextView.text = trip.description
            if (trip.mediaList.size >= 2){
                Glide.with(itemView)
                    .load(trip.mediaList[1])
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .into(photoImageView2)
            }
            if (trip.mediaList.size >= 1){
                Glide.with(itemView)
                    .load(trip.mediaList[0])
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .into(photoImageView1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trip_list_item, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(tripList[position])
    }

    override fun getItemCount(): Int = tripList.size
}