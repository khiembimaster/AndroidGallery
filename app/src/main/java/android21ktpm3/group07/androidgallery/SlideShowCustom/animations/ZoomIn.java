package android21ktpm3.group07.androidgallery.SlideShowCustom.animations;


import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;
import static java.lang.Math.abs;

/**
 * Created by denzcoskun on 03,April,2023.
 * Email: denzcoskun@hotmail.com
 * Istanbul, TURKEY.
 */
public class ZoomIn implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        float scale = position < 0 ? position + 1f : abs(1f - position);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setPivotX(view.getWidth() * 0.5f);
        view.setPivotY(view.getHeight() * 0.5f);
        view.setAlpha(position < -1f || position > 1f ? 0f : 1f - (scale - 1f));
    }
}

