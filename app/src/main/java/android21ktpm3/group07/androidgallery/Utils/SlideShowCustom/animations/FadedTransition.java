package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.animations;

import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;

public class FadedTransition implements PageTransformer {
    private static final float MIN_ALPHA = 0.0f;
    private static final float MIN_SCALE = 0.0f;

    @Override
    public void transformPage(View view, float position) {
        if (position < -1 || position > 1) {
            // Hide the page if it's outside of the visible range
            view.setAlpha(MIN_ALPHA);
            view.setScaleX(MIN_SCALE);
            view.setScaleY(MIN_SCALE);
        } else {
            // Calculate alpha and scale values based on position
            float alpha = 1 - Math.abs(position);
            float scale = 1 - Math.abs(position);

            // Apply alpha and scale to the view
            view.setAlpha(alpha);
            view.setScaleX(scale);
            view.setScaleY(scale);

            // If the page is to the left of center, move it to the left
            if (position < 0) {
                view.setTranslationX(-view.getWidth() * position);
            } else {
                // If the page is to the right of center, move it to the right
                view.setTranslationX(view.getWidth() * -position);
            }
        }
    }
}

