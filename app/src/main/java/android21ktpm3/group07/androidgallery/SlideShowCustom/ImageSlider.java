package android21ktpm3.group07.androidgallery.SlideShowCustom;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.SlideShowCustom.adapters.ViewPagerAdapter;
import android21ktpm3.group07.androidgallery.SlideShowCustom.animations.*;
import android21ktpm3.group07.androidgallery.SlideShowCustom.animations.*;
import android21ktpm3.group07.androidgallery.SlideShowCustom.constants.AnimationTypes;
import android21ktpm3.group07.androidgallery.SlideShowCustom.constants.ScaleTypes;
import android21ktpm3.group07.androidgallery.SlideShowCustom.interfaces.ItemChangeListener;
import android21ktpm3.group07.androidgallery.SlideShowCustom.interfaces.ItemClickListener;
import android21ktpm3.group07.androidgallery.SlideShowCustom.interfaces.TouchListener;
import android21ktpm3.group07.androidgallery.SlideShowCustom.models.SlideModel;



@SuppressLint("ClickableViewAccessibility")
public class ImageSlider extends RelativeLayout {
    private ViewPager viewPager;
    private LinearLayout pagerDots;
    private ViewPagerAdapter viewPagerAdapter;
    private ImageView[] dots;

    private MediaPlayer mediaPlayer;
    private int currentPage = 0;
    private int imageCount = 0;
    private int cornerRadius = 0;
    private long period = 0;
    private long delay = 0;
    private boolean autoCycle = false;
    private int selectedDot = 0;
    private int unselectedDot = 0;
    private int errorImage = 0;
    private int placeholder = 0;
    private int titleBackground = 0;
    private String textAlign = "LEFT";
    private String indicatorAlign = "CENTER";
    private Timer swipeTimer = new Timer();
    private ItemChangeListener itemChangeListener;
    private TouchListener touchListener;

    private String audio;
    private boolean noDots = false;
    private String textColor = "#FFFFFF";
    Context context;
    final Handler handler =new Handler();
    Thread audioThread;


    public ImageSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        LayoutInflater.from(getContext()).inflate(R.layout.image_slider, this, true);
        viewPager = findViewById(R.id.view_pager);
        pagerDots = findViewById(R.id.pager_dots);


        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ImageSlider,
                defStyleAttr,
                defStyleAttr
        );

        mediaPlayer = new MediaPlayer();

        cornerRadius = typedArray.getInt(R.styleable.ImageSlider_iss_corner_radius, 1);
        period = typedArray.getInt(R.styleable.ImageSlider_iss_period, 1000);
        delay = typedArray.getInt(R.styleable.ImageSlider_iss_delay, 1000);
        autoCycle = typedArray.getBoolean(R.styleable.ImageSlider_iss_auto_cycle, false);
        placeholder = typedArray.getResourceId(R.styleable.ImageSlider_iss_placeholder, R.drawable.default_loading);
        errorImage = typedArray.getResourceId(R.styleable.ImageSlider_iss_error_image, R.drawable.default_error);
        selectedDot = typedArray.getResourceId(R.styleable.ImageSlider_iss_selected_dot, R.drawable.default_selected_dot);
        unselectedDot = typedArray.getResourceId(R.styleable.ImageSlider_iss_unselected_dot, R.drawable.default_unselected_dot);
        titleBackground = typedArray.getResourceId(R.styleable.ImageSlider_iss_title_background, R.drawable.default_gradient);
        noDots = typedArray.getBoolean(R.styleable.ImageSlider_iss_no_dots, false);

        if (typedArray.getString(R.styleable.ImageSlider_iss_text_align) != null) {
            textAlign = typedArray.getString(R.styleable.ImageSlider_iss_text_align);
        }

        if (typedArray.getString(R.styleable.ImageSlider_iss_indicator_align) != null) {
            indicatorAlign = typedArray.getString(R.styleable.ImageSlider_iss_indicator_align);
        }

        if (typedArray.getString(R.styleable.ImageSlider_iss_text_color) != null) {
            textColor = typedArray.getString(R.styleable.ImageSlider_iss_text_color);
        }

        typedArray.recycle();
    }

    public ImageSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageSlider(Context context) {
        this(context, null);
    }

    public void setImageList(List<SlideModel> imageList) {
        viewPagerAdapter = new ViewPagerAdapter(context, imageList, cornerRadius, errorImage, placeholder, titleBackground, textAlign, textColor);
        setAdapter(imageList);
    }

    public void setImageList(List<SlideModel> imageList, ScaleTypes scaleType) {
        viewPagerAdapter = new ViewPagerAdapter(context, imageList, cornerRadius, errorImage, placeholder, titleBackground, scaleType, textAlign, textColor);
        setAdapter(imageList);
    }

    public void setImageList(List<SlideModel> imageList, ScaleTypes scaleType, Uri audioUri) {
        viewPagerAdapter = new ViewPagerAdapter(context, imageList, cornerRadius, errorImage, placeholder, titleBackground, scaleType, textAlign, textColor);
        setAdapter(imageList);



    }
    public void startBackgroundMusic(Uri audioUri) {
        if (mediaPlayer != null) {

            mediaPlayer.setLooping(true); // Loop the background music

            // Set audio attributes
            AudioAttributes.Builder audioAttributesBuilder = new AudioAttributes.Builder();
            audioAttributesBuilder.setUsage(AudioAttributes.USAGE_MEDIA);
            audioAttributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
            mediaPlayer.setAudioAttributes(audioAttributesBuilder.build());

            // Set data source
            // mediaPlayer.setDataSource(getContext(), audioUri);
            mediaPlayer=MediaPlayer.create(getContext(), getResources().getIdentifier("abc","raw",getContext().getPackageName()));

            mediaPlayer.start();


        }

//        if (!mediaPlayer.isPlaying()) {
//            mediaPlayer.start();
//        }
    }


    private void setAdapter(List<SlideModel> imageList) {
        viewPager.setAdapter(viewPagerAdapter);
        imageCount = imageList.size();
        if (!imageList.isEmpty()) {
            if (!noDots) {
                setupDots(imageList.size());
            }
            if (autoCycle) {
                startSliding();

            }
        }
    }
    public void setAudio(Uri audioUri){

        this.audio = String.valueOf(audioUri);

                playAudio(audio);


    }
    public void setAudio(String audioUrl){
        this.audio= audioUrl;
    }

    public void setSlideAnimation(AnimationTypes animationType) {
        switch (animationType) {
            case ZOOM_IN:
                viewPager.setPageTransformer(true, new ZoomIn());
                break;
            case ZOOM_OUT:
                viewPager.setPageTransformer(true, new ZoomOut());
                break;
            case DEPTH_SLIDE:
                viewPager.setPageTransformer(true, new DepthSlide());
                break;
            case CUBE_IN:
                viewPager.setPageTransformer(true, new CubeIn());
                break;
            case CUBE_OUT:
                viewPager.setPageTransformer(true, new CubeOut());
                break;
            case FLIP_HORIZONTAL:
                viewPager.setPageTransformer(true, new FlipHorizontal());
                break;
            case FLIP_VERTICAL:
                viewPager.setPageTransformer(true, new FlipVertical());
                break;
            case ROTATE_UP:
                viewPager.setPageTransformer(true, new RotateUp());
                break;
            case ROTATE_DOWN:
                viewPager.setPageTransformer(true, new RotateDown());
                break;
            case FOREGROUND_TO_BACKGROUND:
                viewPager.setPageTransformer(true, new ForegroundToBackground());
                break;
            case BACKGROUND_TO_FOREGROUND:
                viewPager.setPageTransformer(true, new BackgroundToForeground());
                break;
            case TOSS:
                viewPager.setPageTransformer(true, new Toss());
                break;
            case GATE:
                viewPager.setPageTransformer(true, new Gate());
                break;
            default:
                viewPager.setPageTransformer(true, new FidgetSpinner());
                break;
        }
    }

    private void setupDots(int size) {
        pagerDots.setGravity(getGravityFromAlign(indicatorAlign));
        pagerDots.removeAllViews();
        dots = new ImageView[size];

        for (int i = 0; i < size; i++) {
            dots[i] = new ImageView(context);
            dots[i].setImageDrawable(ContextCompat.getDrawable(context, unselectedDot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            pagerDots.addView(dots[i], params);
        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(context, selectedDot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                for (ImageView dot : dots) {
                    dot.setImageDrawable(ContextCompat.getDrawable(context, unselectedDot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(context, selectedDot));
                if (itemChangeListener != null) itemChangeListener.onItemChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void startSliding(long changeablePeriod) {
        stopSliding();
        scheduleTimer(changeablePeriod);
    }

    public void startSliding() {
        startSliding(period);
    }

    public void stopSliding() {
        swipeTimer.cancel();
        swipeTimer.purge();
    }

    private void scheduleTimer(long period) {
        setViewPageScroller(viewPager,new ViewPagerScroller(getContext()));

        Handler handler = new Handler();
        Runnable update = () -> {
            if (currentPage == imageCount) {
                currentPage = 0;
            }
            viewPager.setCurrentItem(currentPage++, true);
        };

        swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, delay, period);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        if (viewPagerAdapter != null) {
            viewPagerAdapter.setItemClickListener(itemClickListener);
        }
    }

    public void setItemChangeListener(ItemChangeListener itemChangeListener) {
        this.itemChangeListener = itemChangeListener;
    }

    public void setTouchListener(TouchListener touchListener) {
        this.touchListener = touchListener;
        if (viewPagerAdapter != null) {
            viewPagerAdapter.setTouchListener(touchListener);
        }
    }
    public void setViewPageScroller(ViewPager view, ViewPagerScroller viewPageScroller) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(view, viewPageScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public int getGravityFromAlign(String textAlign) {
        switch (textAlign) {
            case "RIGHT":
                return Gravity.RIGHT;
            case "LEFT":
                return Gravity.LEFT;
            default:
                return Gravity.CENTER;
        }
    }


    private void playAudio(String audio) {
        // Check if there's an ongoing audio thread and stop it if it exists
        if (audioThread != null && audioThread.isAlive()) {
            audioThread.interrupt();
            audioThread = null;
        }
        audioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final MediaPlayer mediaPlayer = new MediaPlayer();

                Uri myUri = Uri.parse(audio);
                try {
                    mediaPlayer.setDataSource(context, myUri);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        } else {
                            if (mediaPlayer.getCurrentPosition() > 0)
                                mediaPlayer.seekTo(0);
                            mediaPlayer.start();
                        }
                    }
                });
            }
        });

        // Call the start method to start the audio thread
        audioThread.start();
    }


}






