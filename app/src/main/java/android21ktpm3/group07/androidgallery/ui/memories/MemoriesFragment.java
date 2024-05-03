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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import android21ktpm3.group07.androidgallery.databinding.FragmentMemoriesBinding;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class MemoriesFragment extends Fragment {
    private List<Photo> photos;
    private PhotoRepository photoRepository;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    Handler handler = new Handler(Looper.getMainLooper());

    private Runnable updateTask;


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