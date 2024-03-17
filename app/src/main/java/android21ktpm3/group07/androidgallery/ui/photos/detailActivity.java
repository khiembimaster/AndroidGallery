package android21ktpm3.group07.androidgallery.ui.photos;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
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

import android21ktpm3.group07.androidgallery.R;

public class detailActivity extends Dialog {
    private TextView createdDateTextView;
    private TextView byteImageTextView;
    private TextView commentTextView;

    private Image image;


    public detailActivity(Context context) {
        super(context);
        init();
    }

    private void init() {
        // Đặt layout cho Dialog
        setContentView(R.layout.detail_dialog);

        // Tìm các thành phần trong layout của Dialog
        createdDateTextView = findViewById(R.id.createdDate);
        byteImageTextView = findViewById(R.id.byteImage);
        commentTextView = findViewById(R.id.comment);

        // Đặt kích thước và vị trí cho Dialog
        Window window = getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        // Thêm listener cho EditText để theo dõi sự thay đổi của comment
        // Load dữ liệu comment từ SharedPreferences và hiển thị lên EditText
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedComment = sharedPreferences.getString("comment", "");
        commentTextView.setText(savedComment);

        // Thêm listener cho EditText để theo dõi sự thay đổi của comment
        commentTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Khi comment thay đổi, cập nhật dữ liệu trong đối tượng Image và lưu vào SharedPreferences
                if (image != null) {
                    image.setComment(s.toString());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("comment", s.toString());
                    editor.apply();
                }
            }
        });
    }

    public void setData(Image selected) {
        image = selected;
        // Đặt dữ liệu cho các TextView
        createdDateTextView.setText(selected.getCreatedDate());
        byteImageTextView.setText(Double.toString(selected.getByteImg()));
        commentTextView.setText(selected.getComment());
    }


    private void saveCommentData() {
        if (image != null) {
            String comment = commentTextView.getText().toString();
            image.setComment(comment);
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("comment", comment);
            editor.apply();
        }
    }
}
