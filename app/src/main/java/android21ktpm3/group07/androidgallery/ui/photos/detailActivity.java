package android21ktpm3.group07.androidgallery.ui.photos;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.Date;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class detailActivity extends Dialog {
    private TextView createdDateTextView;
    private TextView byteImageTextView;
    private TextView commentTextView;

    long photoDate;
    double photoSize;
    String photoTags;
    String photoPath;

    private final PhotoRepository photoRepository; // Thêm một biến thành viên để lưu trữ tham chiếu của PhotoRepository



    public detailActivity(Context context, PhotoRepository photoRepository) {
        super(context);
        this.photoRepository = photoRepository; // Lưu tham chiếu của PhotoRepository
        init();
    }

    private void init() {
        // Đặt layout cho Dialog
        setContentView(R.layout.detail_dialog);

        // Tìm các thành phần trong layout của Dialog
        createdDateTextView = findViewById(R.id.createdDate);
        byteImageTextView = findViewById(R.id.byteImage);
        commentTextView = findViewById(R.id.comment);


        Window window = getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);



        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedComment = sharedPreferences.getString("comment", "");
        commentTextView.setText(savedComment);


        commentTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (photoRepository != null) {
                        photoRepository.updatePhoto(photoPath,photoTags,System.currentTimeMillis(),photoSize);
                    }
                    return true;
                }
                return false;
            }
        });
        commentTextView.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                photoTags = (s.toString());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("comment", s.toString());
                editor.apply();

            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void setData(String photoPath, long photoDate, double photoSize, String photoTags) {
        Date date = new Date(photoDate);
        String modifiedDate = date.toString();
        this.photoDate = photoDate;
        this.photoSize = photoSize;
        this.photoTags = photoTags;
        this.photoPath = photoPath;
       createdDateTextView.setText(modifiedDate);
        byteImageTextView.setText(Double.toString(photoSize));
        commentTextView.setText(photoTags);



    }


    private void saveCommentData() {

    }
}
