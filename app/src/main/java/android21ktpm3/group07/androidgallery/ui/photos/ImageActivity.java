package android21ktpm3.group07.androidgallery.ui.photos;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.bumptech.glide.Glide;

import java.util.List;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;
import android21ktpm3.group07.androidgallery.ui.editor.PhotoEditor;


public class ImageActivity extends AppCompatActivity {

    private int imageResourceId;

    FrameLayout DetailFrame;
    FrameLayout ImageFrame;

    ImageView display;

    ImageButton edit;
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
    Uri contentUri;
    Photo photo;
    long id;
    private LikedPhotosDatabase database;
    boolean urlExists = false;


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
        contentUri = Uri.parse(getIntent().getStringExtra("photo_contentUri"));

        if (photoPath != null) {
            Glide.with(this)
                    .load(photoPath)
                    .into(display);
        }


        edit = findViewById(R.id.btnEdit);
        share = findViewById(R.id.btnShare);
        delete = findViewById(R.id.btnDelete);
        detail = findViewById(R.id.btnDetail);
        like = findViewById(R.id.btnLike);
        database = Room.databaseBuilder(getApplicationContext(), LikedPhotosDatabase.class,
                        "liked_photos.db")
                .allowMainThreadQueries() // Only for demonstration. In a real app, perform
                // database operations in background threads.
                .build();


        List<LikedPhoto> likedPhotos = database.likedPhotosDao().getAll();


        for (LikedPhoto likedPhoto : likedPhotos) {
            if (likedPhoto.getPhotoUrl().equals(photoPath)) {
                urlExists = true;
                break;
            }
        }

        if (!urlExists) {
            like.setImageResource(R.drawable.like);
        } else {
            like.setImageResource(R.drawable.heart);
        }

        edit.setOnClickListener(view -> {
            Intent intent = new Intent(this, PhotoEditor.class);
            intent.setData(Uri.parse(photoPath));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e("ImageActivity", "Error starting editor activity");
            }
        });

        share.setOnClickListener(view -> {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.setType("image/*");

            startActivity(Intent.createChooser(shareIntent, "Share images to..."));
        });

        delete.setOnClickListener(view -> {
            // Handle delete button click
        });

        detail.setOnClickListener(view -> showDetailDialog());


        like.setOnClickListener(view -> toggleLikeStatus(urlExists));
    }

    private void toggleLikeStatus(boolean isExist) {

        if (!isExist) {
            like.setImageResource(R.drawable.heart);
            addLikedPhotoToDatabase(photoPath);
            Toast.makeText(this, "Added to Liked Photos", Toast.LENGTH_SHORT).show();
        } else {
            like.setImageResource(R.drawable.like);
            removeLikedPhotoFromDatabase(photoPath);
            isExist = false;
            Toast.makeText(this, "Removed from Liked Photos", Toast.LENGTH_SHORT).show();
        }

    }

    private void addLikedPhotoToDatabase(String photoUrl) {
        LikedPhoto likedPhoto = new LikedPhoto(photoUrl);
        database.likedPhotosDao().insert(likedPhoto);
    }

    private void removeLikedPhotoFromDatabase(String photoUrl) {
        database.likedPhotosDao().deleteById(photoUrl);
    }


    private void showDetailDialog() {
        if (photoPath != null) {
            PhotoRepository repository = new PhotoRepository(this);
            detailActivity dialog = new detailActivity(this, repository);
            dialog.setData(photoPath, photoDate, photoSize, photoTags);
            dialog.show();
        }
    }


    public static boolean hasWriteExternalStoragePermission(Context context) {
        int permissionResult = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionResult == PackageManager.PERMISSION_GRANTED;
    }
}
