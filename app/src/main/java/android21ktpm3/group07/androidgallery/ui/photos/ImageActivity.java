package android21ktpm3.group07.androidgallery.ui.photos;


import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
    String photoPath ;
    String photoTags ;
    long photoDate ;
    double photoSize;

    ContentResolver contentResolver;




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
        photoTags = getIntent().getStringExtra("photo_tags") == null ? " " : getIntent().getStringExtra("photo_tags");
        photoDate = getIntent().getLongExtra("photo_date", 0); // 0 là giá trị mặc định nếu không tìm thấy
        photoSize = getIntent().getDoubleExtra("photo_size", 0.0); // 0.0 là giá trị mặc định nếu không tìm thấy






        if (photoPath != null) {

            Glide.with(this)
                    .load(photoPath)
                    .sizeMultiplier(0.5f)
                    .centerCrop()
                    .into(display);
        }





        share = (ImageButton) findViewById(R.id.btnShare);
        delete = (ImageButton) findViewById(R.id.btnDelete);
        detail = (ImageButton) findViewById(R.id.btnDetail);
        like = (ImageButton) findViewById(R.id.btnLike);


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


    public String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
//    public void loadImageFromStorage(String path)
//    {
//
//        try {
//            File f=new File(path, "profile.jpg");
//            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            ImageView img=(ImageView)findViewById(R.id.click_image);
//            img.setImageBitmap(b);
//        }
//        catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//
//    }

}
