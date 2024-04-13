package android21ktpm3.group07.androidgallery.ui.memories;

import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.FitCenterSegment;

public class FitCenterScaleSegment extends FitCenterSegment {
    /**
     * scale range
     */
    private float mScaleFrom;
    private float mScaleTo;

    private float mProgress;

    /**
     * @param duration
     * @param scaleFrom
     * @param scaleTo
     */
    public FitCenterScaleSegment(int duration, float scaleFrom, float scaleTo) {
        super(duration);
        mScaleFrom = scaleFrom;
        mScaleTo = scaleTo;
    }

    @Override
    protected void onDataPrepared() {
        super.onDataPrepared();
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        mProgress = segmentProgress;
        if (!mDataPrepared) {
            return;
        }
        drawBackground(canvas);
        float scale = mScaleFrom + (mScaleTo - mScaleFrom) * mProgress;
        drawContent(canvas, scale);
    }
    @Override
    public void drawContent(GLESCanvas canvas, float scale) {
        super.drawContent(canvas, scale);
    }

    @Override
    public void drawBackground(GLESCanvas canvas) {
        super.drawBackground(canvas);
    }
}