package caruso.example.travelmemoir.ui.tripSearch

import android.util.Log
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
            photoImageView1.visibility = View.GONE
            photoImageView2.visibility = View.GONE


            if (trip.mediaList.size >= 2){
                var pic2 = trip.mediaList[1].replace(Regex("[\\[\\]\\s]"), "")
                Log.e("PICS", pic2)
                photoImageView2.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(pic2)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .into(photoImageView2)
            }
            if (trip.mediaList.size >= 1){
                var pic1 = trip.mediaList[0].replace(Regex("[\\[\\]\\s]"), "")
                photoImageView1.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(pic1)
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