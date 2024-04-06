package android21ktpm3.group07.androidgallery.SlideShowCustom.animations;


import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;


public class RotateUp implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        int width = view.getWidth();
        int height = view.getHeight();
        float rotation = -15f * position * -1.25f;

        view.setPivotX(width * 0.5f);
        view.setPivotY(height);
        view.setRotation(rotation);
    }
}
