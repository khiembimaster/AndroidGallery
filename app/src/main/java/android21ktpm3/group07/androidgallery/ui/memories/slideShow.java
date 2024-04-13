package android21ktpm3.group07.androidgallery.ui.memories;

import static android.content.ContentValues.TAG;
import static android.widget.ImageView.ScaleType.CENTER_CROP;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.hw.photomovie.util.ScaleType;

import java.util.ArrayList;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.SlideShowCustom.ImageSlider;
import android21ktpm3.group07.androidgallery.SlideShowCustom.constants.AnimationTypes;
import android21ktpm3.group07.androidgallery.SlideShowCustom.constants.ScaleTypes;
import android21ktpm3.group07.androidgallery.SlideShowCustom.interfaces.ItemChangeListener;
import android21ktpm3.group07.androidgallery.SlideShowCustom.interfaces.ItemClickListener;
import android21ktpm3.group07.androidgallery.SlideShowCustom.models.SlideModel;
import android21ktpm3.group07.androidgallery.ui.memories.widget.MovieBottomView;
import android21ktpm3.group07.androidgallery.ui.memories.widget.MovieFilterView;
import android21ktpm3.group07.androidgallery.ui.memories.widget.MovieTransferView;

/**
 * Created by Deniz Coşkun on 6/23/2020.
 * denzcoskun@hotmail.com
 * İstanbul
 */

public class slideShow extends AppCompatActivity {
    private static final int SELECT_PICTURE = 100;
    int PICK_IMAGE_MULTIPLE = 1;
    ArrayList<String> imagePathSelected = new ArrayList<>();

    private ActivityResultLauncher<Intent> imageChooserLauncher;

    private View addView;
    private View mFloatAddView;


    private MovieFilterView btnScale;
    private MovieTransferView btnAnimation;
    private MovieBottomView btnMusic;
    ArrayList<SlideModel> imageList = new ArrayList<>(); // Create image list
    LinearLayout movieAddLayout;

    ImageSlider imageSlider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slide_show_custom);
        imageSlider = findViewById(R.id.image_slider);

        movieAddLayout = findViewById(R.id.movie_add);

        // btnScale = findViewById(R.id.scale);




       if(imagePathSelected.isEmpty()){
          imageSlider.setVisibility(View.GONE);

       }

       movieAddLayout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               handlePermission();
               openImageChooser();
            //   addImage(imagePathSelected);

           }
       });
//        btnAnimation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Hiển thị PopupMenu khi người dùng nhấn vào nút animation
//                showAnimationPopupMenu();
//            }
//        });

      //  imageSlider.setImageList(imageList, ScaleTypes.CENTER_CROP);

        imageSlider.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int position) {
                // You can listen here.
                System.out.println("normal");
            }

            @Override
            public void doubleClick(int position) {
                // Do not use onItemSelected if you are using a double click listener at the same time.
                // Its just added for specific cases.
                // Listen for clicks under 250 milliseconds.
                System.out.println("its double");
            }
        });

        imageSlider.setItemChangeListener(new ItemChangeListener() {
            @Override
            public void onItemChanged(int position) {
                //System.out.println("Pos: " + position);
            }
        });

//        imageSlider.setTouchListener(new TouchListener() {
//            @Override
//            public void onTouched(ActionTypes touched, int position) {
//                if (touched == ActionTypes.DOWN) {
//                   imageSlider.stopSliding();
//                } else if (touched == ActionTypes.UP) {
//                   imageSlider.startSliding(1000);
//                }
//            }
//        });
        imageChooserLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() ==  RESULT_OK && result.getData() != null ) {

                        Intent data = result.getData();
                    if (data.getClipData() != null) {
                        System.out.println("vô đây k")     ;
                        int cout = data.getClipData().getItemCount();
                        for (int i = 0; i < cout; i++) {
                            Uri imageuri = data.getClipData().getItemAt(i).getUri();
                            imageList.add(new SlideModel(imageuri, "",ScaleTypes.CENTER_CROP));

                        }


//                        imageSlider.setVisibility(View.VISIBLE);
//                        movieAddLayout.setVisibility(View.GONE);
//                        mFloatAddView.setVisibility(View.GONE);

                    } else {
                        Uri imageuri = data.getData();
                        imageList.add(new SlideModel(imageuri, "",ScaleTypes.CENTER_CROP));


                    }
                }else {
                    // show this if no image is selected
                    Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
                }
            imageSlider.setImageList(imageList,ScaleTypes.CENTER_CROP);
            imageSlider.setSlideAnimation(AnimationTypes.ROTATE_DOWN);
        });
    }

    private void showAnimationPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, btnAnimation);
        popupMenu.getMenu().add("DEPTH_SLIDE");
        popupMenu.getMenu().add("CUBE_IN");
        popupMenu.getMenu().add("CUBE_OUT");
        popupMenu.getMenu().add("FLIP_HORIZONTAL");
        popupMenu.getMenu().add("FLIP_VERTICAL");
        popupMenu.getMenu().add("FLIP_VERTICAL");
        popupMenu.getMenu().add("FOREGROUND_TO_BACKGROUND");
        popupMenu.getMenu().add("BACKGROUND_TO_FOREGROUND");
        popupMenu.getMenu().add("ROTATE_UP");
        popupMenu.getMenu().add("ROTATE_DOWN");
        popupMenu.getMenu().add("GATE");
        popupMenu.getMenu().add("TOSS");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {


            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().isEmpty()){
                    imageSlider.setSlideAnimation(AnimationTypes.DEPTH_SLIDE);
                        return true;
                }

                imageSlider.setSlideAnimation(AnimationTypes.valueOf(item.getTitle().toString()));
                return true;

            }
        });

        popupMenu.show();
    }


    private void handlePermission() {


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PICK_IMAGE_MULTIPLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SELECT_PICTURE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                        if (showRationale) {
                            //  Show your own message here
                        } else {
                            showSettingsAlert();
                        }
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* Choose an image from Gallery */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imageChooserLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }






    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        openAppSettings(slideShow.this);
                    }
                });
        alertDialog.show();
    }

    public static void openAppSettings(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

}
