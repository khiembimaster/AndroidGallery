package android21ktpm3.group07.androidgallery.ui.memories.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import android21ktpm3.group07.androidgallery.R;


/**
 * Created by huangwei on 2018/9/9.
 */
public class MovieBottomView extends ConstraintLayout implements View.OnClickListener {

    private MovieBottomCallback mCallback;

    public MovieBottomView(Context context) {
        super(context);
    }

    public MovieBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovieBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.movie_next).setOnClickListener(this);
        findViewById(R.id.movie_transfer).setOnClickListener(this);
        findViewById(R.id.movie_transfer_txt).setOnClickListener(this);
        findViewById(R.id.movie_music).setOnClickListener(this);
        findViewById(R.id.movie_music_txt).setOnClickListener(this);
    }

    public void setCallback(MovieBottomCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case 1000060:
            case  1000005:
                if(mCallback!=null){
                    mCallback.onFilterClick();
                }
                break;
            case  1000031:
            case  1000019:
                if(mCallback!=null){
                    mCallback.onTransferClick();
                }
                break;
            case 1000010:
            case 1000015:
                if(mCallback!=null){
                    mCallback.onMusicClick();
                }
                break;
            case  1000044:
                if(mCallback!=null){
                    mCallback.onNextClick();
                }
                break;
        }
    }

    public static interface MovieBottomCallback{
        void onNextClick();
        void onMusicClick();
        void onTransferClick();
        void onFilterClick();
    }
}