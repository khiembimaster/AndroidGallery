package android21ktpm3.group07.androidgallery.ui.photos;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android21ktpm3.group07.androidgallery.R;

public class ImageActivity extends AppCompatActivity {

    private int imageResourceId;

    FrameLayout DetailFrame;
    FrameLayout ImageFrame;


    ImageView display;

    ImageButton share;

    ImageButton like;

    ImageButton detail;

    ImageButton delete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image);
        int imageResourceId = getIntent().getIntExtra("selected_image", 0);



        display = findViewById(R.id.image_item);
      //  DetailFrame = findViewById(R.id.content_frame);
        ImageFrame = findViewById(R.id.image_frame);

        ImageFrame.setVisibility(View.VISIBLE);
       // DetailFrame.setVisibility(View.GONE);

        // Hiển thị ảnh từ đối tượng Image lên ImageView
        if (imageResourceId != 0) {
            display.setImageResource(imageResourceId);
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


//        detail.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////
////                DetailFrame.setVisibility(View.VISIBLE);
////
////                DetailFragment newFragment = new DetailFragment();
////                getSupportFragmentManager().beginTransaction()
////                        .replace(R.id.content_frame, newFragment)
////
////                        .addToBackStack(null) // Để có thể quay lại khi ấn nút back
////                        .commit();
////            }
//        });


        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


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
