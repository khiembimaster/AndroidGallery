package android21ktpm3.group07.androidgallery.ui.memories;

import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.TransitionSegment;

public class GradientTransferSegment extends TransitionSegment<FitCenterScaleSegment, FitCenterScaleSegment> {

    private float mPreScaleFrom;
    private float mPreScaleTo;
    private float mNextScaleFrom;
    private float mNextScaleTo;

    public GradientTransferSegment(int duration,
                                   float preScaleFrom, float preScaleTo,
                                   float nextScaleFrom, float nextScaleTo) {
        mPreScaleFrom = preScaleFrom;
        mPreScaleTo = preScaleTo;
        mNextScaleFrom = nextScaleFrom;
        mNextScaleTo = nextScaleTo;
        setDuration(duration);
    }

    @Override
    protected void onDataPrepared() {

    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        //zoom in animation
        float nextScale = mNextScaleFrom + (mNextScaleTo - mNextScaleFrom) * segmentProgress;
        mNextSegment.drawContent(canvas, nextScale);

        //zoom out & alpha animation
        float preScale = mPreScaleFrom + (mPreScaleTo - mPreScaleFrom) * segmentProgress;
        float alpha = 1 - segmentProgress;
        mPreSegment.drawBackground(canvas);
        canvas.save();
        canvas.setAlpha(alpha);
        mPreSegment.drawContent(canvas, preScale);
        canvas.restore();
    }
}