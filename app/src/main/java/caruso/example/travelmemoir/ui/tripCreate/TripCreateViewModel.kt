package caruso.example.travelmemoir.ui.tripCreate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TripCreateViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is record trip Fragment"
    }
    val text: LiveData<String> = _text
}