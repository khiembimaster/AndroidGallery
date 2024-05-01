package android21ktpm3.group07.androidgallery.ui.editor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.BrushSettingsBottomSheetLayoutBinding;
import android21ktpm3.group07.androidgallery.databinding.FragmentBottomSheetBinding;

public class BrushSettingsBottomSheet extends BottomSheetDialogFragment {
    BrushSettingsBottomSheetLayoutBinding binding;
    PhotoEditor photoEditor;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BrushSettingsBottomSheetLayoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        photoEditor = (PhotoEditor) requireActivity();

        binding.seekBarBrushSize.addOnChangeListener(photoEditor);
        binding.seekBarOpacity.addOnChangeListener(photoEditor);
        return root;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        photoEditor.applyShapeBuilder();
    }
}
