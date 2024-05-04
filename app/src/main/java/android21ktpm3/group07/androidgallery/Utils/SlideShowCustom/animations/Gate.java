package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.animations;




import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;
import static java.lang.Math.abs;


public class Gate implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        view.setTranslationX(-position * view.getWidth());

        if (position < -1) {
            view.setAlpha(0f);
        } else if (position <= 0) {
            view.setAlpha(1f);
            view.setPivotX(0f);
            view.setRotationY(90 * abs(position));
        } else if (position <= 1) {
            view.setAlpha(1f);
            view.setPivotX(view.getWidth());
            view.setRotationY(-90 * abs(position));
        } else {
            view.setAlpha(0f);
        }
    }
}
