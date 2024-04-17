package android21ktpm3.group07.androidgallery.ui.memories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MemoriesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MemoriesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is memories fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}