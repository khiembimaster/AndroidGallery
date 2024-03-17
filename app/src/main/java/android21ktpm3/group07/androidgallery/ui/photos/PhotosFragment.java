package android21ktpm3.group07.androidgallery.ui.photos;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import android21ktpm3.group07.androidgallery.databinding.FragmentPhotosBinding;

public class PhotosFragment extends Fragment {
    private FragmentPhotosBinding binding;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private PhotoAdapter photoAdapter;
    PhotosRecyclerAdapter photosRecyclerAdapter;


    public ArrayList<Image> imgList;
    // Khai báo ActivityResultLauncher
    private ActivityResultLauncher<Intent> imageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    System.out.println("cách mới");
                    // Xử lý kết quả ở đây
                    Intent data = result.getData();
                    //Bundle extras = data.getExtras();
                   // Image selectedImage = extras.getParcelable("updated_comment");
                   // System.out.println(selectedImage.getComment());
                    if (data != null ) {
                       String updatedComment = data.getStringExtra("updated_comment");
                      Image selectedImage = data.getParcelableExtra("selected_image");

                        selectedImage.setComment(updatedComment);

                        // Xác định vị trí của selectedImage trong danh sách dữ liệu
                        int position = imgList.indexOf(selectedImage);
                        imgList.set(1, selectedImage);

                        System.out.println(imgList.get(1).getByteImg());
                        System.out.println(imgList.get(1).getComment());
                        updateImageList(imgList);
                        if (position != -1) {
                            // Cập nhật selectedImage với dữ liệu mới


                            // Cập nhật dữ liệu trong RecyclerView
                            imgList.set(position, selectedImage);
                            adapter.notifyItemChanged(position);
                        }
                    }
                }
            }
    );

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PhotosViewModel photosViewModel =
                new ViewModelProvider(this).get(PhotosViewModel.class);

        binding = FragmentPhotosBinding.inflate(inflater, container, false);
        PhotosRecyclerAdapter photosRecyclerAdapter = new PhotosRecyclerAdapter(getContext(), this);

        imgList = photosRecyclerAdapter.getImgList();
        View root = binding.getRoot();


        layoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(new PhotosRecyclerAdapter(getContext(),this));


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void startImageActivity(Intent intent) {

        // Khởi chạy hoạt động với ActivityResultLauncher
        imageActivityResultLauncher.launch(intent);
    }
    public void updateImageList(ArrayList<Image> img) {
        ArrayList<Image> imageList = null;

        imageList.addAll(img);
        photosRecyclerAdapter.updateImgList(imageList);

        // Gọi phương thức cập nhật Adapter
    }


}