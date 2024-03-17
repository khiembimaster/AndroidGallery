package android21ktpm3.group07.androidgallery.ui.photos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import android21ktpm3.group07.androidgallery.databinding.FragmentPhotosBinding;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PhotosFragment extends Fragment {
    private FragmentPhotosBinding binding;
    private PhotosViewModel photosViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
        photosViewModel.setPhotoRepository(new PhotoRepository(this.getActivity()));
        photosViewModel.setUpdateRunnable(UpdateRecyclerView);
        photosViewModel.loadPhotos();


        binding = FragmentPhotosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerView.setLayoutManager(layoutManager);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public Runnable UpdateRecyclerView = new Runnable() {
        @Override
        public void run() {
            binding.recyclerView.setAdapter(new PhotosRecyclerAdapter(
                    getActivity(),
                    photosViewModel.getPhotosGroupByDate()
            ));
        }
    };
}