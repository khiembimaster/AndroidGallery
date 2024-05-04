package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.animations;




import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;


public class CubeIn implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        view.setPivotX(position > 0 ? 0f : view.getWidth());
        view.setPivotY(0f);
        view.setRotationY(-90f * position);
    }
}