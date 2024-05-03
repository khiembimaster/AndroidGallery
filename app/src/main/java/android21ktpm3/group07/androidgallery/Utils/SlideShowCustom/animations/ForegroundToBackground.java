package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.animations;




import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;
import static java.lang.Math.abs;
import static java.lang.Math.min;


public class ForegroundToBackground implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        float height = (float) view.getHeight();
        float width = (float) view.getWidth();
        float scale = Math.min((position > 0) ? 1f : abs(1f + position), 1f);

        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setPivotX(width * 0.5f);
        view.setPivotY(height * 0.5f);
        view.setTranslationX((position > 0) ? width * position : -width * position * 0.25f);
    }
}