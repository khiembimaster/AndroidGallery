package android21ktpm3.group07.androidgallery.ui.SlideShow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SlideShowViewModel extends ViewModel {

    private static MutableLiveData<String> mText = null;

    public SlideShowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is search fragment");
    }

    public static LiveData<String> getText() {
        return mText;
    }
}
