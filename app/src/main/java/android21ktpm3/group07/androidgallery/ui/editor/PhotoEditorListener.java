package android21ktpm3.group07.androidgallery.ui.editor;

import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.mrudultora.colorpicker.ColorPickerDialog;
import com.mrudultora.colorpicker.listeners.OnSelectColorListener;
import com.mrudultora.colorpicker.util.ColorItemShape;

import android21ktpm3.group07.androidgallery.R;
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.ViewType;

public class PhotoEditorListener implements OnPhotoEditorListener, OnSelectColorListener {
    PhotoEditor mphotoEditor;
    RelativeLayout mSurfaceView;
    EditText mEditText;
    Button mButton;
    ColorPickerDialog colorPickerDialog;
    public PhotoEditorListener(PhotoEditor photoEditor, RelativeLayout surfaceView, EditText editText, Button button){
        mphotoEditor = photoEditor;
        mEditText = editText;
        mSurfaceView = surfaceView;
        mButton = button;
    }
    @Override
    public void onAddViewListener(@NonNull ViewType viewType, int i) {

    }

    @Override
    public void onEditTextChangeListener(@NonNull View view, @NonNull String s, int i) {
        mSurfaceView.setVisibility(View.VISIBLE);
        mEditText.setText(s);
        mEditText.setTextColor(i);
        mButton.setOnClickListener(v -> {
            mphotoEditor.editText(view, mEditText.getText().toString(), mEditText.getCurrentTextColor());
            mSurfaceView.setVisibility(View.GONE);
        });
    }

    @Override
    public void onRemoveViewListener(@NonNull ViewType viewType, int i) {

    }

    @Override
    public void onStartViewChangeListener(@NonNull ViewType viewType) {

    }

    @Override
    public void onStopViewChangeListener(@NonNull ViewType viewType) {

    }

    @Override
    public void onTouchSourceImage(@NonNull MotionEvent motionEvent) {

    }

    public void setColorPickerDialog(ColorPickerDialog colorPickerDialog){
        this.colorPickerDialog = colorPickerDialog;
    }
    @Override
    public void onColorSelected(int color, int position) {
        // handle color or position
        mEditText.setTextColor(color);
    }

    @Override
    public void cancel() {
        colorPickerDialog.dismissDialog();	// Dismiss the dialog.
    }
}
