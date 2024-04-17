package android21ktpm3.group07.androidgallery.ui.memories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android21ktpm3.group07.androidgallery.databinding.FragmentMemoriesBinding;

public class MemoriesFragment extends Fragment {

    private FragmentMemoriesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MemoriesViewModel MemoriesViewModel =
                new ViewModelProvider(this).get(MemoriesViewModel.class);

        binding = FragmentMemoriesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textMemories;
        MemoriesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}