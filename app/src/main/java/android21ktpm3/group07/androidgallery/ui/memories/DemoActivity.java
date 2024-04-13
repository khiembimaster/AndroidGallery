package android21ktpm3.group07.androidgallery.ui.memories;


import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.modernstorage.photopicker.PhotoPicker;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.model.PhotoSource;
import com.hw.photomovie.render.GLTextureView;

import com.hw.photomovie.segment.MovieSegment;
import com.hw.photomovie.util.AppResources;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.ui.memories.widget.FilterItem;
import android21ktpm3.group07.androidgallery.ui.memories.widget.MovieBottomView;
import android21ktpm3.group07.androidgallery.ui.memories.widget.MovieFilterView;
import android21ktpm3.group07.androidgallery.ui.memories.widget.MovieTransferView;
import android21ktpm3.group07.androidgallery.ui.memories.widget.TransferItem;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2018/9/9.
 */
public class DemoActivity extends AppCompatActivity implements IDemoView, MovieBottomView.MovieBottomCallback {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private ActivityResultLauncher<Intent> musicPickerLauncher;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int REQUEST_MUSIC = 234;
    private static final int REQUEST_SELECT_PHOTOS = 123; // Định nghĩa mã yêu cầu chọn ảnh

    private DemoPresenter mDemoPresenter = new DemoPresenter();
    private GLTextureView mGLTextureView;
    private MovieFilterView mFilterView;
    private MovieTransferView mTransferView;
    private MovieBottomView mBottomView;
    private View mSelectView;
    private List<FilterItem> mFilters;
    private List<TransferItem> mTransfers;
    private View mFloatAddView;
    private List<Photo> memoriesPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppResources.getInstance().init(getResources());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo);



        memoriesPhoto = getIntent().getParcelableArrayListExtra("memoriesPhotos");


        mGLTextureView = findViewById(R.id.gl_texture);
        mBottomView = findViewById(R.id.movie_bottom_layout);
        mSelectView = findViewById(R.id.movie_add);
        mFloatAddView = findViewById(R.id.movie_add_float);
        mDemoPresenter.attachView(this);
        mBottomView.setCallback(this);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPhotos();
            }
        };
        photoPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK ) {
                Intent data = result.getData();
                if (data != null) {
                    if (data.getData() != null) {
                        // Xử lý khi chỉ chọn một ảnh
                        Uri selectedImageUri = data.getData();
                        Bitmap bitmap = null;
                        try {
                             bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        ArrayList<String> photos = new ArrayList<>();
                        photos.add("drawable://" + R.drawable.avatar01);
                        mDemoPresenter.onPhotoPick(photos);
                    } else if (data.getClipData() != null) {
                        // Xử lý khi chọn nhiều ảnh
                        ClipData clipData = data.getClipData();
                        ArrayList<String> photos = new ArrayList<>();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri selectedImageUri = clipData.getItemAt(i).getUri();
                            String picturePath = "file://"  + "/storage/emulated/0/Pictures/IMG_20240403_114347.jpg";
                            photos.add(picturePath);

                        }
                        mDemoPresenter.onPhotoPick(photos);
                    }
                    mFloatAddView.setVisibility(View.VISIBLE);
                    mSelectView.setVisibility(View.GONE);
                }
            }
        });

        musicPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri uri = data.getData();
                    mDemoPresenter.setMusic(uri);
                }
            }
        });




        mSelectView.setOnClickListener(onClickListener);
        mFloatAddView.setOnClickListener(onClickListener);

    }
    public String getRealPathFromURI(Uri contentURI, Activity context) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = context.managedQuery(contentURI, projection, null,
                null, null);
        if (cursor == null)
            return null;
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        if (cursor.moveToFirst()) {
            String s = cursor.getString(column_index);
            // cursor.close();
            return s;
        }
        // cursor.close();
        return null;
    }

    private void requestPhotos() {

//        PhotoPickerIntent intent = new PhotoPickerIntent(DemoActivity.this);
//        intent.setPhotoCount(9);
//        intent.setShowCamera(true);
//        intent.setShowGif(true);
//        startActivityForResult(intent, REQUEST_CODE);
//        PhotoPicker.
//        PhotoPicker.builder()
//                .setPhotoCount(9)
//                .setShowCamera(false)
//                .setShowGif(false)
//                .setPreviewEnabled(true)
//                .start(this, PhotoPicker.REQUEST_CODE);





        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Cho phép chọn nhiều ảnh
        photoPickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));    }


    @Override
    public GLTextureView getGLView() {
        return mGLTextureView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDemoPresenter.detachView();
    }

    private boolean checkInit() {
        if (mSelectView.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "please select photos", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    @Override
    public void onNextClick() {
        if (checkInit()) {
            return;
        }
        mDemoPresenter.saveVideo();
    }

    @Override
    public void onMusicClick() {
        if (checkInit()) {
            return;
        }
        Intent i = new Intent();
        i.setType("audio/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        musicPickerLauncher.launch(i);
    }

    @Override
    public void onTransferClick() {
        if (checkInit()) {
            return;
        }
        if (mTransferView == null) {
            ViewStub stub = findViewById(R.id.movie_menu_transfer_stub);
            mTransferView = (MovieTransferView) stub.inflate();
            mTransferView.setVisibility(View.GONE);
            mTransferView.setItemList(mTransfers);
            mTransferView.setTransferCallback(mDemoPresenter);
        }
        mBottomView.setVisibility(View.GONE);
        mTransferView.show();
    }

    @Override
    public void onFilterClick() {
        if (checkInit()) {
            return;
        }
        if (mFilterView == null) {
            ViewStub stub = findViewById(R.id.movie_menu_filter_stub);
            mFilterView = (MovieFilterView) stub.inflate();
            mFilterView.setVisibility(View.GONE);
            mFilterView.setItemList(mFilters);
            mFilterView.setFilterCallback(mDemoPresenter);
        }
        mBottomView.setVisibility(View.GONE);
        mFilterView.show();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && requestCode == REQUEST_MUSIC) {
//            Uri uri = data.getData();
//            mDemoPresenter.setMusic(uri);
//        } else if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_PHOTOS) {
//            if (data != null) {
//                ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
//                mDemoPresenter.onPhotoPick(photos);
//                mFloatAddView.setVisibility(View.VISIBLE);
//                mSelectView.setVisibility(View.GONE);
//            }
//        }
//        if (resultCode == RESULT_OK && requestCode == REQUEST_MUSIC) {
//            Uri uri = data.getData();
//            mDemoPresenter.setMusic(uri);
//        } else if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_PHOTOS) {
//            if (data != null) {
//                if (data.getData() != null) {
//                    // Xử lý khi chỉ chọn một ảnh
//                    Uri selectedImageUri = data.getData();
//                    ArrayList<String> photos = new ArrayList<>();
//                    photos.add(selectedImageUri.toString());
//                    mDemoPresenter.onPhotoPick(photos);
//                } else if (data.getClipData() != null) {
//                    // Xử lý khi chọn nhiều ảnh
//                    ClipData clipData = data.getClipData();
//                    ArrayList<String> photos = new ArrayList<>();
//                    for (int i = 0; i < clipData.getItemCount(); i++) {
//                        Uri selectedImageUri = clipData.getItemAt(i).getUri();
//                        photos.add(selectedImageUri.toString());
//                    }
//                    mDemoPresenter.onPhotoPick(photos);
//                }
//                mFloatAddView.setVisibility(View.VISIBLE);
//                mSelectView.setVisibility(View.GONE);
//            }
//        }

  //  }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mFilterView != null && mFilterView.getVisibility() == View.VISIBLE
                    && !checkInArea(mFilterView, ev)) {
                mFilterView.hide();
                mBottomView.setVisibility(View.VISIBLE);
                return true;
            } else if (mTransferView != null && mTransferView.getVisibility() == View.VISIBLE
                    && !checkInArea(mTransferView, ev)) {
                mTransferView.hide();
                mBottomView.setVisibility(View.VISIBLE);
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean checkInArea(View view, MotionEvent event) {
        int loc[] = new int[2];
        view.getLocationInWindow(loc);
        return event.getRawY() > loc[1];
    }

    @Override
    public void setFilters(List<FilterItem> filters) {
        mFilters = filters;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void setTransfers(List<TransferItem> items) {
        mTransfers = items;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDemoPresenter.onPause();
        mGLTextureView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDemoPresenter.onResume();
        mGLTextureView.onResume();
    }
    private static PhotoMovie initGradientPhotoMovie(PhotoSource photoSource) {
        List<MovieSegment> segmentList = new ArrayList<>(photoSource.size());
        for (int i = 0; i < photoSource.size(); i++) {
            if (i == 0) {
                segmentList.add(new FitCenterScaleSegment(1600, 1f, 1.1f));
            } else {
                segmentList.add(new FitCenterScaleSegment(1600, 1.05f, 1.1f));
            }
            if (i < photoSource.size() - 1) {
                segmentList.add(new GradientTransferSegment(800, 1.1f, 1.15f, 1.0f, 1.05f));
            }
        }
        return new PhotoMovie(photoSource, segmentList);
    }
}