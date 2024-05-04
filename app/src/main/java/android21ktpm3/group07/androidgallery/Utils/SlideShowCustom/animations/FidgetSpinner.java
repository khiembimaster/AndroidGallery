package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.animations;




import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;
import static java.lang.Math.abs;


public class FidgetSpinner implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        view.setTranslationX(-position * view.getWidth());

        if (abs(position) < 0.5) {
            view.setVisibility(View.VISIBLE);
            view.setScaleX(1 - abs(position));
            view.setScaleY(1 - abs(position));
        } else if (abs(position) > 0.5) {
            view.setVisibility(View.GONE);
        }

        if (position < -1) {
            view.setAlpha(0f);
        } else if (position <= 0) {
            view.setAlpha(1f);
            view.setRotation(36000 * (abs(position) * abs(position) * abs(position) * abs(position) * abs(position) * abs(position) * abs(position)));
        } else if (position <= 1) {
            view.setAlpha(1f);
            view.setRotation(-36000 * (abs(position) * abs(position) * abs(position) * abs(position) * abs(position) * abs(position) * abs(position)));
        } else {
            view.setAlpha(0f);
        }
    }
}
