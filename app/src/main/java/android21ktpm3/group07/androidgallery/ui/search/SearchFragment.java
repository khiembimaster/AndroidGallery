package android21ktpm3.group07.androidgallery.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.UserViewModel;
import android21ktpm3.group07.androidgallery.databinding.FragmentSearchBinding;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SearchViewModel SearchViewModel;
    private SearchResultRVAdapter resultRVAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SearchViewModel =
                new ViewModelProvider(this).get(SearchViewModel.class);

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<String> labelList = Arrays.asList(getResources().getStringArray(R.array.labels));
        final ListView searchSuggestionsListView = binding.searchSuggestions;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_list_item_1, new ArrayList<>());
        searchSuggestionsListView.setAdapter(adapter);
        binding.searchBar.setText("Search for images by label");

        resultRVAdapter = new SearchResultRVAdapter(requireContext(), SearchViewModel.getPhotoResults().getValue());
        binding.searchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.setAdapter(resultRVAdapter);


        SearchViewModel.getSearchText().observe(getViewLifecycleOwner(), searchText -> {
            binding.searchView.getEditText().setText(searchText);
            binding.searchBar.setText(searchText);
        });

        SearchViewModel.getSearchResults().observe(getViewLifecycleOwner(), searchResults -> {
            adapter.clear();
            adapter.addAll(searchResults);
            adapter.notifyDataSetChanged();
        });

        SearchViewModel.getPhotoResults().observe(getViewLifecycleOwner(), photos -> {
            resultRVAdapter.setPhotos(photos);
        });


        searchSuggestionsListView.setOnItemClickListener((parent, view, position, id) -> {
            TextView textView = (TextView) view;
            EditText searchText = binding.searchView.getEditText();
            searchText.setText(textView.getText());
            SearchViewModel.updateSearchText(textView.getText().toString());
            // Update search results
            db.collection("users").document(user.getUid()).collection("images")
                    .whereArrayContains("tags", textView.getText().toString())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Photo> photos = new ArrayList<>();
                        for(QueryDocumentSnapshot query : queryDocumentSnapshots) {
                            Map<String, Object> result = query.getData();
                            String url = (String) result.get("url");
                            String name = (String) result.get("name");
                            // Date modifiedDate = (Date) result.get("modifiedDate");
                            List<String> tags = (List<String>) result.get("tags");

                            Photo photo = new Photo(url, name, null, tags, 0.0);
                            photos.add(photo);
                        }
                        SearchViewModel.updatePhotoResults(photos);
                    });
        });

        final EditText searchText = binding.searchView.getEditText();
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                SearchViewModel.updateSearchResults(s.toString(), labelList);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}