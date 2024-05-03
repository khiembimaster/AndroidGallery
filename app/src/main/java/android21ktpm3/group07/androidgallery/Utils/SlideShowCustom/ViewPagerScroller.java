package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom;





import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;



public class ViewPagerScroller extends Scroller {

    private int fixedDuration = 1000; // Time to scroll in milliseconds

    public ViewPagerScroller(Context context) {
        super(context);
    }

    public ViewPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, fixedDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, fixedDuration);
    }
}
