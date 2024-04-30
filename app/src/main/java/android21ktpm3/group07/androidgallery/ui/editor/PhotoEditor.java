package android21ktpm3.group07.androidgallery.ui.editor;

import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationBarView;
import com.mrudultora.colorpicker.ColorPickerDialog;
import com.mrudultora.colorpicker.listeners.OnSelectColorListener;
import com.mrudultora.colorpicker.util.ColorItemShape;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.ActivityPhotoEditorBinding;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;

public class PhotoEditor extends AppCompatActivity {
    ActivityPhotoEditorBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityPhotoEditorBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PhotoEditorView mPhotoEditorView = binding.photoEditorView;
        ImageView imageView = mPhotoEditorView.getSource();
        Uri uri = getIntent().getData();
        imageView.setImageURI(uri);


        //Use custom font using latest support library
        Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);

//loading font from asset
//         Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

        ja.burhanrashid52.photoeditor.PhotoEditor mPhotoEditor = new ja.burhanrashid52.photoeditor.PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .setClipSourceImage(true)
                .setDefaultTextTypeface(mTextRobotoTf)
                // .setDefaultEmojiTypeface(mEmojiTypeFace)
                .build();

        // ShapeBuilder mShapeBuilder = new ShapeBuilder()
        //         .withShapeOpacity(100)
        //         .withShapeSize(50f);
        //
        // mPhotoEditor.setShape(mShapeBuilder);

        mPhotoEditor.setBrushDrawingMode(true);

        mPhotoEditor.setOnPhotoEditorListener(new PhotoEditorListener(mPhotoEditor,
                binding.addTextSurfaceView,
                binding.addTextEditText,
                binding.saveChangesButton,
                binding.openColorDialogButton
        ));





        binding.bottomEditAction.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if(itemId == R.id.action_brush) {
                    mPhotoEditor.setBrushDrawingMode(true);
                    return true;
                } else if (itemId == R.id.action_text) {
                    mPhotoEditor.addText("Hello World", Color.YELLOW);
                    return true;
                } else if (itemId == R.id.action_erase) {
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

    }
}