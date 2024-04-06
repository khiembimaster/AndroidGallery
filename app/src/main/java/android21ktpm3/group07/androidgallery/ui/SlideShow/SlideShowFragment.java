package android21ktpm3.group07.androidgallery.ui.SlideShow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import android21ktpm3.group07.androidgallery.databinding.FragmentSearchBinding;
import android21ktpm3.group07.androidgallery.ui.memories.slideShow;
import android21ktpm3.group07.androidgallery.ui.search.SearchViewModel;

public class SlideShowFragment extends Fragment {

    private FragmentSearchBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideShowViewModel slideShowViewModel = new ViewModelProvider(this).get(SlideShowViewModel.class);

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Intent intent = new Intent(requireContext(), SlideShowActivity.class);
        startActivity(intent);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
