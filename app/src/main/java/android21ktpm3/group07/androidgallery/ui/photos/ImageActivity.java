package android21ktpm3.group07.androidgallery.ui.photos;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    Image selectedImage;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

                Intent intent = new Intent(this, PhotoAdapter.class);
                intent.putExtra("updated_comment",selectedImage.getComment());
                intent.putExtra("selected_image", selectedImage);

            setResult(RESULT_OK, intent);
                System.out.println(intent);
                finish();


            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

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
        selectedImage = getIntent().getParcelableExtra("selected_image");



        display = findViewById(R.id.image_item);


        if (selectedImage != null) {
            display.setImageResource(selectedImage.getImage());
            System.out.println(selectedImage.getComment());
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
        // Khởi tạo CustomDialog mới
        detailActivity dialog = new detailActivity(this);

        // Đặt dữ liệu cho Dialog
        dialog.setData(selectedImage);

        // Hiển thị Dialog
        dialog.show();
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
