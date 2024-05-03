package android21ktpm3.group07.androidgallery.helpers;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import android21ktpm3.group07.androidgallery.R;

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

    @BindingAdapter("animate")
    public static <T extends ISelectable> void setAnimation(View view, T selectable) {
        if (selectable.isSelected()) {
            Animation scaleDown = AnimationUtils.loadAnimation(view.getContext(),
                    R.anim.scale_down);
            scaleDown.setFillEnabled(true);
            scaleDown.setFillAfter(true);

            if (!selectable.isAnimated()) {
                scaleDown.setDuration(0);
            }

            view.startAnimation(scaleDown);
        } else {
            Animation scaleUp = AnimationUtils.loadAnimation(view.getContext(), R.anim.scale_up);
            scaleUp.setFillEnabled(true);
            scaleUp.setFillAfter(true);

            if (!selectable.isAnimated()) {
                scaleUp.setDuration(0);
            }

            view.startAnimation(scaleUp);
        }
        selectable.setAnimated(false);
    }
}
