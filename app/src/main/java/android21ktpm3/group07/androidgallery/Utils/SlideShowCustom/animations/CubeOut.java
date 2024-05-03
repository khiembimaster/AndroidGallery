package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.animations;




import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;


public class CubeOut implements PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        view.setPivotX(position < 0f ? view.getWidth() : 0f);
        view.setPivotY(view.getHeight() * 0.5f);
        view.setRotationY(90f * position);
    }
}
