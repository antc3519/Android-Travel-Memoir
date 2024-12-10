package caruso.example.travelmemoir

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import caruso.example.travelmemoir.ui.friends.FriendsFragment
import caruso.example.travelmemoir.ui.home.HomeFragment
import caruso.example.travelmemoir.ui.settings.SettingsFragment
import caruso.example.travelmemoir.ui.tripCreate.TripCreateFragment
import caruso.example.travelmemoir.ui.tripSearch.TripSearchFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentManager: FragmentManager = supportFragmentManager

        // define your fragments here
        val fragment1: Fragment = TripCreateFragment(0.0, 0.0)
        val fragment2: Fragment = TripSearchFragment("")
        val fragment3: Fragment = HomeFragment()
        val fragment4: Fragment = FriendsFragment()
        val fragment5: Fragment = SettingsFragment()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // handle navigation selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            lateinit var fragment: Fragment
            when (item.itemId) {
                R.id.document_trip -> fragment = fragment1
                R.id.search_trip -> fragment = fragment2
                R.id.globe_view -> fragment = fragment3
                R.id.friends_view -> fragment = fragment4
                R.id.settings_view -> fragment = fragment5
            }
            fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit()
            Log.e("NAV", "onCreate: SWITCHED", )
            true

        }

        // Set default selection
        bottomNavigationView.selectedItemId = R.id.globe_view
    }
}