package android21ktpm3.group07.androidgallery.ui.memories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class CustomVideoView extends View {

    private Bitmap[] frames;
    private int currentFrameIndex = 0;
    private Paint paint;
    private Handler handler;
    private Runnable frameUpdater;

    public CustomVideoView(Context context) {
        super(context);
        init();
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        handler = new Handler();
        frameUpdater = new Runnable() {
            @Override
            public void run() {
                invalidate();
                handler.postDelayed(this, 1000 / 30); // Update frames at 30 fps
            }
        };
    }

    public void setFrames(Bitmap[] frames) {
        this.frames = frames;
        currentFrameIndex = 0;
        startPlayback();
    }

    public void startPlayback() {
        handler.post(frameUpdater);
    }

    public void stopPlayback() {
        handler.removeCallbacks(frameUpdater);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (frames != null && frames.length > 0) {
            Bitmap currentFrame = frames[currentFrameIndex];
            canvas.drawBitmap(currentFrame, 0, 0, paint);
            currentFrameIndex = (currentFrameIndex + 1) % frames.length;
        }
    }
}
