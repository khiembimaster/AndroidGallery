package android21ktpm3.group07.androidgallery.SlideShowCustom.animations;


import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;


public class RotateDown implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        int width = view.getWidth();
        float rotation = -15f * position;

        view.setPivotX(width * 0.5f);
        view.setPivotY(0f);
        view.setTranslationX(0f);
        view.setRotation(rotation);
    }
}
