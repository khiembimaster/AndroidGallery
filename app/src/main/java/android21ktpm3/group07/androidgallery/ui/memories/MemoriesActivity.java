package android21ktpm3.group07.androidgallery.ui.memories;




import static android.content.ContentValues.TAG;
import static android.widget.ImageView.ScaleType.CENTER_CROP;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



import java.util.ArrayList;
import java.util.List;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.ImageSlider;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.constants.ActionTypes;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.constants.AnimationTypes;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.constants.ScaleTypes;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.interfaces.ItemChangeListener;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.interfaces.ItemClickListener;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.interfaces.TouchListener;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.models.SlideModel;
import android21ktpm3.group07.androidgallery.models.Photo;

public class MemoriesActivity extends AppCompatActivity {
    Context context;
    private static final int SELECT_PICTURE = 100;
    int PICK_IMAGE_MULTIPLE = 1;
    ArrayList<String> imagePathSelected = new ArrayList<>();

    private ActivityResultLauncher<Intent> imageChooserLauncher;

    private ActivityResultLauncher<Intent> audioChooserLauncher;


    ImageView movieMusicImageView;

    ImageView movieTransferImageView ;
    ArrayList<SlideModel> imageList = new ArrayList<>(); // Create image list
    LinearLayout movieAddLayout;

    ImageSlider imageSlider;
    private final int PICK_AUDIO = 1;

    private List<Photo> memoriesPhoto;

    public MemoriesActivity() {

    }

    public MemoriesActivity(Context context, List<Photo> memoriesPhoto) {
        this.context = context;
        this.memoriesPhoto = memoriesPhoto;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memories_activity);
        imageSlider = findViewById(R.id.memories_show);

        context = this;


        memoriesPhoto = getIntent().getParcelableArrayListExtra("memoriesPhotos");

        for (int i = 0; i < memoriesPhoto.size(); i++) {
            Uri imageuri = memoriesPhoto.get(i).getContentUri();
            imageList.add( new SlideModel(imageuri, "", ScaleTypes.CENTER_CROP));

        }
        imageSlider.setImageList(imageList,ScaleTypes.CENTER_CROP);
        imageSlider.setSlideAnimation(AnimationTypes.FADE);

        MediaPlayer music = MediaPlayer.create(MemoriesActivity.this, R.raw.memoriesaudio);
        imageSlider.setAudio(String.valueOf(R.raw.memoriesaudio));

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
            }
        });

        imageSlider.setTouchListener(new TouchListener() {
            @Override
            public void onTouched(ActionTypes touched, int position) {
                if (touched == ActionTypes.DOWN) {
                    imageSlider.stopSliding();
                } else if (touched == ActionTypes.UP) {
                    imageSlider.startSliding(1000);
                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri selectedAudioUri = data.getData();
                    // Handle the selected audio URI
                    // For example, you can use it to play the selected audio or perform any other operation
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Handle the case when the user canceled the action
            }
        }
    }
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.animation_menu, popupMenu.getMenu());

        // Thiết lập lắng nghe cho các mục menu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getTitle() == null){

                    return false;
                }
                imageSlider.setSlideAnimation(AnimationTypes.valueOf((String) item.getTitle()));

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
                        openAppSettings(android21ktpm3.group07.androidgallery.ui.memories.MemoriesActivity.this);
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
