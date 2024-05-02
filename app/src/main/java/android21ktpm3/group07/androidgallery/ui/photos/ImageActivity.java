package android21ktpm3.group07.androidgallery.ui.photos;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.room.Room;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;


public class ImageActivity extends AppCompatActivity {

    private int imageResourceId;

    FrameLayout DetailFrame;
    FrameLayout ImageFrame;

    ImageView display;

    ImageButton share;
    ImageButton like;
    ImageButton detail;
    ImageButton delete;

    String photoName;
    String photoPath;
    String photoTags;
    long photoDate;
    double photoSize;
    String isFavourite;
    String takenDate;
    Photo photo;
    long id;
    private LikedPhotosDatabase database;


    ContentResolver contentResolver;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;


    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Lifecycle", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", "onDestroy");
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
        takenDate = getIntent().getStringExtra("photo_takenDate");
        isFavourite = getIntent().getStringExtra("photo_isFavourite");
        id = getIntent().getLongExtra("photo_id", 0);

        if (photoPath != null) {
            Glide.with(this)
                    .load(photoPath)
                    .into(display);
        }

        share = findViewById(R.id.btnShare);
        delete = findViewById(R.id.btnDelete);
        detail = findViewById(R.id.btnDetail);
        like = findViewById(R.id.btnLike);

        if (Objects.equals(isFavourite, "0")) {
            like.setImageResource(R.drawable.like);
        } else {
            like.setImageResource(R.drawable.heart);
        }

        share.setOnClickListener(view -> {
            // Handle share button click
        });

        delete.setOnClickListener(view -> {
            // Handle delete button click
        });

        detail.setOnClickListener(view -> showDetailDialog());

        database = Room.databaseBuilder(getApplicationContext(), LikedPhotosDatabase.class, "liked_photos.db")
                .allowMainThreadQueries() // Only for demonstration. In a real app, perform database operations in background threads.
                .build();

        like.setOnClickListener(view -> toggleLikeStatus());
    }
    private void toggleLikeStatus() {
        if (Objects.equals(isFavourite, "0")) {
            like.setImageResource(R.drawable.heart);
            isFavourite = "1";
            addLikedPhotoToDatabase(photoPath);
            Toast.makeText(this, "Added to Liked Photos", Toast.LENGTH_SHORT).show();
        } else {
            like.setImageResource(R.drawable.like);
            isFavourite = "0";
            removeLikedPhotoFromDatabase(photoPath);
            Toast.makeText(this, "Removed from Liked Photos", Toast.LENGTH_SHORT).show();
        }
        List<LikedPhoto> likedPhotos = database.likedPhotosDao().getAll();

    }

    private void addLikedPhotoToDatabase(String photoUrl) {
        LikedPhoto likedPhoto = new LikedPhoto(photoUrl);
        database.likedPhotosDao().insert(likedPhoto);
    }

    private void removeLikedPhotoFromDatabase(String photoUrl) {
        LikedPhoto likedPhoto = new LikedPhoto(photoUrl);
        database.likedPhotosDao().delete(likedPhoto);
    }




    private void showDetailDialog() {
        if (photoPath != null) {
            PhotoRepository repository = new PhotoRepository(contentResolver);
            detailActivity dialog = new detailActivity(this, repository);
            dialog.setData(photoPath, photoDate, photoSize, photoTags);
            dialog.show();
        }
    }

    private void updateMediaStore(File favoriteFile) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, favoriteFile.getAbsolutePath());
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        String newStatus = Objects.equals(isFavourite, "0") ? "1" : "0";
        values.put(MediaStore.Images.Media.IS_FAVORITE, newStatus);
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{favoriteFile.getAbsolutePath()};
        int rowsUpdated = getContentResolver().update(uri, values, selection, selectionArgs);
        Log.d("Update", "Rows updated: " + rowsUpdated);
    }

    public void updatePhotoFavouriteStatus(long photoId, String isFavourite) {
        ContentValues values = new ContentValues();
        String newStatus = Objects.equals(isFavourite, "0") ? "1" : "0";
        values.put(MediaStore.Images.Media.IS_FAVORITE, newStatus);
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{photoPath};

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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        int rowsUpdated = contentResolver.update(collection, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            Log.d("Update", "Rows updated: " + rowsUpdated);
        } else {
            Log.d("Update", "No rows updated");
        }
    }



    public static boolean hasWriteExternalStoragePermission(Context context) {
        int permissionResult = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionResult == PackageManager.PERMISSION_GRANTED;
    }
}
