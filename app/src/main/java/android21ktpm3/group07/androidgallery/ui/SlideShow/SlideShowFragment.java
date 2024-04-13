//package android21ktpm3.group07.androidgallery.ui.SlideShow;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//
//
//import android21ktpm3.group07.androidgallery.R;
//import android21ktpm3.group07.androidgallery.SlideShowCustom.ImageSlider;
//import android21ktpm3.group07.androidgallery.SlideShowCustom.constants.ActionTypes;
//import android21ktpm3.group07.androidgallery.SlideShowCustom.interfaces.ItemChangeListener;
//import android21ktpm3.group07.androidgallery.SlideShowCustom.interfaces.ItemClickListener;
//import android21ktpm3.group07.androidgallery.SlideShowCustom.interfaces.TouchListener;
//import android21ktpm3.group07.androidgallery.databinding.FragmentSlideshowBinding;
//
//
//public class SlideShowFragment extends Fragment {
//
//    private @NonNull FragmentSlideshowBinding binding;
//    private SlideShowActivityInterface slideShowActivityInterface;
//
//    private ImageView movieMusicImageView;
//    private LinearLayout movieAddLayout;
//    private ImageView movieTransferImageView;
//    private ImageSlider imageSlider;
//
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        SlideShowViewModel slideShowViewModel = new ViewModelProvider(this).get(SlideShowViewModel.class);
//
//        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        FrameLayout slideShowContainer = root.findViewById(R.id.slide_show_container);
//        imageSlider = root.findViewById(R.id.image_slider);
//        movieAddLayout = root.findViewById(R.id.movie_add);
//        movieMusicImageView = root.findViewById(R.id.movie_music);
//        movieTransferImageView = root.findViewById(R.id.movie_transfer);
//
//        setupClickListeners();
//
//
//        return root;
//    }
//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        if (context instanceof SlideShowActivityInterface) {
//            slideShowActivityInterface = (SlideShowActivityInterface) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement SlideShowActivityInterface");
//        }
//    }
//
//
//    private void setupClickListeners() {
//        movieMusicImageView.setOnClickListener(v -> slideShowActivityInterface.openAudioPicker());
//        movieAddLayout.setOnClickListener(v -> {
//       //     slideShowActivityInterface.handlePermission(getContext());
//            slideShowActivityInterface.openImageChooser();
//        });
//        movieTransferImageView.setOnClickListener(v -> slideShowActivityInterface.showPopupMenu(imageSlider,v));
//        imageSlider.setItemClickListener(new ItemClickListener() {
//            @Override
//            public void onItemSelected(int position) {
//            }
//
//            @Override
//            public void doubleClick(int position) {
//            }
//        });
//
//        imageSlider.setItemChangeListener(new ItemChangeListener() {
//            @Override
//            public void onItemChanged(int position) {
//            }
//        });
//
//        imageSlider.setTouchListener(new TouchListener() {
//            @Override
//            public void onTouched(ActionTypes touched, int position) {
//                if (touched == ActionTypes.DOWN) {
//                    imageSlider.stopSliding();
//                } else if (touched == ActionTypes.UP) {
//                    imageSlider.startSliding(1000);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//    public void setActivityInterface(SlideShowActivityInterface activityInterface) {
//        this.slideShowActivityInterface = activityInterface;
//    }
//}
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