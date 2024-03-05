package android21ktpm3.group07.androidgallery.ui.photos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PhotosViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public PhotosViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is photos fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}