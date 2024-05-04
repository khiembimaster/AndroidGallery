package android21ktpm3.group07.androidgallery.ui.search;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchViewModel extends ViewModel {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> mSearchText;
    private final MutableLiveData<List<String>> searchResults;
    private final MutableLiveData<List<Photo>> photoResults;

    public SearchViewModel() {
        mText = new MutableLiveData<>();
        mSearchText = new MutableLiveData<>();
        searchResults = new MutableLiveData<>();
        photoResults = new MutableLiveData<>();
        photoResults.setValue(new ArrayList<Photo>());
    }

    public MutableLiveData<String> getText() {
        return mText;
    }

    public MutableLiveData<List<String>> getSearchResults() {
        return searchResults;
    }

    public MutableLiveData<String> getSearchText() {
        return mSearchText;
    }

    public MutableLiveData<List<Photo>> getPhotoResults() {
        return photoResults;
    }

    public void updateSearchText(String query) {
        mSearchText.setValue(query);
    }

    public void updateSearchResults(String query, List<String> allItems) {
        if (query.isEmpty()) {
            return;
        }
        List<String> filteredItems = allItems.stream()
                .filter(item -> item.toLowerCase().startsWith(query.toLowerCase()))
                .collect(Collectors.toList());
        searchResults.setValue(filteredItems);
    }

    public void updatePhotoResults(List<Photo> photos) {
        photoResults.setValue(photos);
    }

}