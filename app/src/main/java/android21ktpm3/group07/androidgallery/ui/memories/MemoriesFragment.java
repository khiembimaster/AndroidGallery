package android21ktpm3.group07.androidgallery.ui.memories;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import android21ktpm3.group07.androidgallery.databinding.FragmentMemoriesBinding;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;
import android21ktpm3.group07.androidgallery.ui.photos.PhotosRecyclerAdapter;
import android21ktpm3.group07.androidgallery.ui.photos.PhotosViewModel;


public class MemoriesFragment extends Fragment {

    private FragmentMemoriesBinding binding;
    protected MemoriesViewModel memoriesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMemoriesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize MemoriesViewModel
        memoriesViewModel = new ViewModelProvider(this).get(MemoriesViewModel.class);
        memoriesViewModel.setPhotoRepository(new PhotoRepository(requireContext())); // Use requireContext() instead of this.getActivity()
        memoriesViewModel.setUpdateTask(UpdateRecyclerView);

        // Load photos
        memoriesViewModel.loadPhotos();

        return root;
    }

    // Method to start MemoriesActivity
    public Runnable UpdateRecyclerView = new Runnable() {
        @Override
        public void run() {
            // Check if photos are loaded successfully
            if (memoriesViewModel.getPhotosGroupByDatePrevious() != null) {
                // Start MemoriesActivity
                startMemoriesActivity();
            } else {
                // Handle the case where photos are null
                Log.e("MemoriesFragment", "Failed to load photos");
            }
        }
    };

    // Method to start MemoriesActivity
    private void startMemoriesActivity() {
        // Post the action to start MemoriesActivity on the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    Intent intent = new Intent(requireContext(), MemoriesActivity.class);
                    intent.putExtra("memoriesPhotos", new ArrayList<>(memoriesViewModel.getPhotosGroupByDatePrevious()));
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

