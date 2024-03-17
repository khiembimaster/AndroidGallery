//package android21ktpm3.group07.androidgallery.ui.photos;
//
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.ArrayList;
//
//import android21ktpm3.group07.androidgallery.R;
//import android21ktpm3.group07.androidgallery.ui.photos.PhotoAdapter;
//
//public class ResultActivityPhotoAdapter extends AppCompatActivity implements PhotoAdapter.OnActivityResultListener {
//    private PhotoAdapter photoAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.images_by_date_layout);
//
//        // Initialize your RecyclerView and adapter
//        photoAdapter = new PhotoAdapter(this,new ArrayList<>(), this);
//        RecyclerView recyclerView = findViewById(R.id.image_by_date_recycler_view); // Thay thế "recyclerView" bằng ID của RecyclerView trong layout của bạn
//        recyclerView.setAdapter(photoAdapter);
//        System.out.println("tới đây không");
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        System.out.println("check1");
//
//        if (requestCode == PhotoAdapter.REQUEST_IMAGE_ACTIVITY && resultCode == RESULT_OK) {
//            photoAdapter.onActivityResult(requestCode, resultCode, data);
//            System.out.println("tới đây không");
//
//        }
//    }
//}
