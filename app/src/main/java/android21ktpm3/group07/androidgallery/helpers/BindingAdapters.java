package android21ktpm3.group07.androidgallery.helpers;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class BindingAdapters {
    @BindingAdapter(
            value = {"url", "error", "sizeMultiplier", "centerCrop"},
            requireAll = false)
    public static void loadImage(
            ImageView imageView, String url, Drawable error, Float sizeMultiplier,
            Boolean centerCrop
    ) {
        RequestOptions options = new RequestOptions();
        if (error != null) options = options.error(error);
        if (sizeMultiplier != null) options = options.sizeMultiplier(sizeMultiplier);
        if (centerCrop != null && centerCrop) options = options.centerCrop();

        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(options)
                .into(imageView);
    }

    @BindingAdapter("android:visibility")
    public static void setVisibility(View view, Boolean value) {
        view.setVisibility(value ? View.VISIBLE : View.GONE);
    }
}
