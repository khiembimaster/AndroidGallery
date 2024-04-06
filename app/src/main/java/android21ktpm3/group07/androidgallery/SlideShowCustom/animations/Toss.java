package android21ktpm3.group07.androidgallery.SlideShowCustom.animations;


import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;
import static java.lang.Math.abs;
import static java.lang.Math.max;


public class Toss implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        view.setTranslationX(-position * view.getWidth());
        view.setCameraDistance(20000f);

        if (position < 0.5 && position > -0.5) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }

        if (position < -1) {
            view.setAlpha(0f);
        } else if (position <= 0) {
            view.setAlpha(1f);
            view.setScaleX(max(0.4f, 1 - abs(position)));
            view.setScaleY(max(0.4f, 1 - abs(position)));
            view.setRotationX(1080 * (1 - abs(position) + 1));
            view.setTranslationY(-1000 * abs(position));
        } else if (position <= 1) {
            view.setAlpha(1f);
            view.setScaleX(max(0.4f, 1 - abs(position)));
            view.setScaleY(max(0.4f, 1 - abs(position)));
            view.setRotationX(-1080 * (1 - abs(position) + 1));
            view.setTranslationY(-1000 * abs(position));
        } else {
            view.setAlpha(0f);
        }
    }
}

