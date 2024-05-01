package android21ktpm3.group07.androidgallery.ui.editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.slider.Slider;
import com.google.type.DateTime;
import com.mrudultora.colorpicker.ColorPickerDialog;
import com.mrudultora.colorpicker.listeners.OnSelectColorListener;
import com.mrudultora.colorpicker.util.ColorItemShape;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import android21ktpm3.group07.androidgallery.BottomSheetFragment;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.ActivityPhotoEditorBinding;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.SaveSettings;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;

public class PhotoEditor extends AppCompatActivity implements View.OnClickListener, Slider.OnChangeListener {
    ActivityPhotoEditorBinding binding;
    PhotoEditorListener photoEditorListener;
    ja.burhanrashid52.photoeditor.PhotoEditor mPhotoEditor;
    ShapeBuilder shapeBuilder;
    BrushSettingsBottomSheet brushSettingsBottomSheet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityPhotoEditorBinding.inflate(getLayoutInflater());
        brushSettingsBottomSheet = new BrushSettingsBottomSheet();

        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PhotoEditorView mPhotoEditorView = binding.photoEditorView;

        Uri uri = getIntent().getData();
        ImageView imageView = mPhotoEditorView.getSource();

        Glide.with(this)
                .asBitmap()
                .load(uri.getPath())
                .into(new CustomTarget<Bitmap>(){

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Bitmap bitmap = transformBitmapTo2048px(resource);
                        imageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });


        //Use custom font using latest support library
        Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);

        mPhotoEditor = new ja.burhanrashid52.photoeditor.PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .setClipSourceImage(true)
                .setDefaultTextTypeface(mTextRobotoTf)
                // .setDefaultEmojiTypeface(mEmojiTypeFace)
                .build();

        shapeBuilder = new ShapeBuilder();
        binding.fabBrushSettings.setOnClickListener(v -> {
            brushSettingsBottomSheet.show(getSupportFragmentManager(), brushSettingsBottomSheet.getTag());
        });
        binding.fabColorPalette.setOnClickListener(this);

        mPhotoEditor.setBrushDrawingMode(true);
        photoEditorListener = new PhotoEditorListener(mPhotoEditor,
                binding.addTextSurfaceView,
                binding.addTextEditText,
                binding.saveChangesButton
        );
        mPhotoEditor.setOnPhotoEditorListener(photoEditorListener);
        binding.openColorDialogButton.setOnClickListener(this);

        binding.bottomEditAction.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if(itemId == R.id.action_brush) {
                    binding.fabColorPalette.setVisibility(View.VISIBLE);
                    binding.fabBrushSettings.setVisibility(View.VISIBLE);
                    mPhotoEditor.setBrushDrawingMode(true);
                    return true;
                } else if (itemId == R.id.action_text) {
                    binding.fabColorPalette.setVisibility(View.GONE);
                    binding.fabBrushSettings.setVisibility(View.GONE);
                    mPhotoEditor.addText("Hello World", Color.BLACK);
                    return true;
                } else if (itemId == R.id.action_erase) {
                    binding.fabColorPalette.setVisibility(View.GONE);
                    binding.fabBrushSettings.setVisibility(View.VISIBLE);
                    mPhotoEditor.brushEraser();
                    return true;
                }
                    // else if (itemId == R.id.action_emoji) {
                //     mPhotoEditor.addEmoji("\uD83D\uDE03");
                //     return true;
                // } else if (itemId == R.id.action_undo) {
                //     mPhotoEditor.undo();
                //     return true;
                // } else if (itemId == R.id.action_redo) {
                //     mPhotoEditor.redo();
                //     return true;
                // }


                return false;
            }
        });

        binding.closeEditorButton.setOnClickListener(v -> {
            finish();
        });


        binding.saveImageButton.setOnClickListener(v -> {
            SaveSettings saveSettings = new SaveSettings.Builder()
                    .setClearViewsEnabled(true)
                    .setTransparencyEnabled(true)
                    .setCompressFormat(Bitmap.CompressFormat.PNG)
                    .setCompressQuality(100)
                    .build();




            mPhotoEditor.saveAsFile(uri.getPath(),saveSettings, new ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(@NonNull String imagePath) {
                    Log.e("PhotoEditor","Image Saved Successfully at " + imagePath);
                    Toast.makeText(PhotoEditor.this, "Image Saved Successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("PhotoEditor","Failed to save Image");
                    Toast.makeText(PhotoEditor.this, "Failed to save Image", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onClick(View v) {
        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(v.getContext());   // Pass the context.
        colorPickerDialog.show();	// Show the dialog.
        colorPickerDialog.setColors()
                .setColumns(4)                        		// Default number of columns is 5.
                .setDefaultSelectedColor(1)		// By default no color is used.
                .setColorItemShape(ColorItemShape.CIRCLE);
        int action = binding.bottomEditAction.getSelectedItemId();
        if(action == R.id.action_text){
            photoEditorListener.setColorPickerDialog(colorPickerDialog);
            colorPickerDialog.setOnSelectColorListener(photoEditorListener);
        } else if(action == R.id.action_brush){
            colorPickerDialog.setOnSelectColorListener(new OnSelectColorListener() {
                @Override
                public void onColorSelected(int color, int position) {
                    shapeBuilder.withShapeColor(color);
                    applyShapeBuilder();
                }

                @Override
                public void cancel() {
                    colorPickerDialog.dismissDialog();
                }
            });
        }
    }

    @Override
    public void onValueChange(@NonNull Slider slider, float v, boolean b) {
        if(slider.getId() == R.id.seekBarBrushSize){
            // shapeBuilder.withShapeType(ShapeType);
            shapeBuilder.withShapeSize(v );
        } else if (slider.getId() == R.id.seekBarOpacity){
            shapeBuilder.withShapeOpacity((int)v);
        }
    }

    public void applyShapeBuilder(){
        mPhotoEditor.setShape(shapeBuilder);
    }
    public static Bitmap transformBitmapTo2048px(Bitmap source){
        if(source.getHeight() <= 2048 && source.getWidth() <= 2048)
            return source;

        int targetWidth;
        int targetHeight;

        double aspectRatio = (double) source.getHeight() / (double) source.getWidth();

        if(source.getWidth() >= source.getHeight()){
            targetWidth = 2048;
            targetHeight = (int)(2048 * aspectRatio);
        } else {
            targetHeight = 2048;
            targetWidth = (int)(2048 / aspectRatio);
        }

        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
        if (result != source) {
            // Same bitmap is returned if sizes are the same
            source.recycle();
        }
        return result;
    }
}