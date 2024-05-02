package android21ktpm3.group07.androidgallery.ui.photos;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class ImageActivity extends AppCompatActivity {

    private int imageResourceId;

    FrameLayout DetailFrame;
    FrameLayout ImageFrame;

  //  private final Context context;


    ImageView display;

    ImageButton share;

    ImageButton like;

    ImageButton detail;

    ImageButton delete;

    String photoName;
    String photoPath ;
    String photoTags ;
    long photoDate ;
    double photoSize;
    String isFavourite;

    String takenDate;
    Photo photo;
    long id;

    ContentResolver contentResolver;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;





    @Override
    protected void onStop() {

        super.onStop();
        System.out.println("stop");
    }
    @Override
    protected void onDestroy() {

        super.onDestroy();
        System.out.println("destroy");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image);
        display = findViewById(R.id.image_item);

        contentResolver = getContentResolver();

        photoPath = getIntent().getStringExtra("photo_path");
        photoTags = getIntent().getStringExtra("photo_tags");
        photoDate = getIntent().getLongExtra("photo_date", 0);
        photoSize = getIntent().getDoubleExtra("photo_size", 0.0);
        photoName = getIntent().getStringExtra("photo_name");
        takenDate =  getIntent().getStringExtra("photo_takenDate");
        isFavourite =  getIntent().getStringExtra("photo_isFavourite");
        id =  getIntent().getLongExtra("photo_id",0);

        //  isFavourite = photo.getIsFavourite();
       // id = photo.getId();




        if (photoPath != null) {
            Glide.with(this)
                    .load(photoPath)
                    .into(display);
        }





        share = (ImageButton) findViewById(R.id.btnShare);
        delete = (ImageButton) findViewById(R.id.btnDelete);
        detail = (ImageButton) findViewById(R.id.btnDetail);
        like = (ImageButton) findViewById(R.id.btnLike);
        System.out.println("favourite" + isFavourite);
        if (Objects.equals(isFavourite, "0")) {
            like.setImageResource(R.drawable.like);
        } else {
            like.setImageResource(R.drawable.heart);
        }


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDetailDialog();
            }
        });


        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePhotoFavouriteStatus(id, isFavourite);
                saveToFavorites(photoPath);
            }
        });


    }
    private void showDetailDialog() {
        if (photoPath != null){
            PhotoRepository repository = new PhotoRepository(contentResolver);

            detailActivity dialog = new detailActivity(this,repository);



            // Đặt dữ liệu cho Dialog

            dialog.setData(photoPath,photoDate,photoSize,photoTags);

            // Hiển thị Dialog
            dialog.show();
        }

    }

    private void saveToFavorites(String photoPath) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File favoritesDirectory = new File(picturesDirectory, "Favorites");
            if (!favoritesDirectory.exists()) {
                if (!favoritesDirectory.mkdirs()) {
                    Toast.makeText(this, "Failed to create Favorites directory", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Glide.with(this)
                    .asBitmap()
                    .load(photoPath)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            saveImageInBackground(favoritesDirectory, resource);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            // Nothing to do here
                        }
                    });
        } else {
            Toast.makeText(this, "External storage not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageInBackground(File favoritesDirectory, Bitmap bitmap) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("name photo" + photoName);
                File favoriteFile = new File(favoritesDirectory, photoName);
                try {
                    OutputStream fos = new FileOutputStream(favoriteFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, favoriteFile.getAbsolutePath());
                    values.put(MediaStore.Images.Media.DATE_MODIFIED, photoDate / 1000);
                    values.put(MediaStore.Images.Media.DATE_TAKEN, takenDate);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ImageActivity.this, "Image saved to Favorites", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ImageActivity.this, "Failed to save image to Favorites", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    public void updatePhotoFavouriteStatus(long photoId, String isFavourite) {
        ContentValues values = new ContentValues();
        String newStatus = Objects.equals(isFavourite, "0") ? "1" : "0";
        values.put(MediaStore.Images.Media.IS_FAVORITE, newStatus);

        // Use _ID for selection
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{photoPath};

        // Ensure contentResolver is not null and handle the case where it is
        if (contentResolver == null) {
            Log.e("Update", "ContentResolver is null, cannot update photo favorite status");
            return;
        }
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Nếu ứng dụng chưa được cấp quyền, yêu cầu quyền từ người dùng
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        System.out.println(hasWriteExternalStoragePermission(this));
        int rowsUpdated = contentResolver.update(
                collection,
                values,
                selection,
                selectionArgs
        );


        // Check if any rows were updated
        if (rowsUpdated > 0) {
            Log.d("Update", "Rows updated: " + rowsUpdated);
        } else {
            Log.d("Update", "No rows updated");
        }
    }
    public static boolean hasWriteExternalStoragePermission(Context context) {
        // Kiểm tra xem ứng dụng có quyền WRITE_EXTERNAL_STORAGE hay không
        int permissionResult = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionResult == PackageManager.PERMISSION_GRANTED;
    }





}
