package caruso.example.travelmemoir.ui.tripSearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TripSearchViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is search trip Fragment"
    }
    val text: LiveData<String> = _text
}