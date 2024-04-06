package android21ktpm3.group07.androidgallery.SlideShowCustom.animations;


import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;


public class FlipVertical implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        float rotation = -180f * position;

        view.setAlpha((rotation > 90f || rotation < -90f) ? 0f : 1f);
        view.setPivotX(view.getWidth() * 0.5f);
        view.setPivotY(view.getHeight() * 0.5f);
        view.setRotationX(rotation);
    }
}
