package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.adapters;



import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.constants.ActionTypes;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.constants.ScaleTypes;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.interfaces.ItemClickListener;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.interfaces.TouchListener;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.models.SlideModel;
import android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.transformations.RoundedTransformation;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private List<SlideModel> imageList;
    private int radius;
    private int errorImage;
    private int placeholder;
    private int titleBackground;
    private ScaleTypes scaleType;
    private String textAlign;
    private String textColor;

    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private TouchListener touchListener;
    private long lastTouchTime;
    private long currentTouchTime;

    public ViewPagerAdapter(Context context, List<SlideModel> imageList, int radius, int errorImage,
                            int placeholder, int titleBackground, ScaleTypes scaleType, String textAlign, String textColor) {
        this.context = context;
        this.imageList = imageList;
        this.radius = radius;
        this.errorImage = errorImage;
        this.placeholder = placeholder;
        this.titleBackground = titleBackground;
        this.scaleType = scaleType;
        this.textAlign = textAlign;
        this.textColor = textColor;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ViewPagerAdapter(Context context, List<SlideModel> imageList, int radius, int errorImage,
                            int placeholder, int titleBackground, String textAlign, String textColor) {
        this(context, imageList, radius, errorImage, placeholder, titleBackground, null, textAlign, textColor);
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.pager_row, container, false);

        ImageView imageView = itemView.findViewById(R.id.image_view);
        LinearLayout linearLayout = itemView.findViewById(R.id.linear_layout);
        TextView textView = itemView.findViewById(R.id.text_view);
        textView.setTextColor(Color.parseColor(textColor));

        if (imageList.get(position).getTitle() != null) {
            textView.setText(imageList.get(position).getTitle());
            linearLayout.setBackgroundResource(titleBackground);
            textView.setGravity(getGravityFromAlign(textAlign));
            linearLayout.setGravity(getGravityFromAlign(textAlign));
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
        }

        // Image from url or local path check.
        RequestCreator loader;
        if (imageList.get(position).getImageUrl() == null && imageList.get(position).getImageUri() == null) {
            loader = Picasso.get().load(imageList.get(position).getImagePath());
        } else if (imageList.get(position).getImageUri() == null && imageList.get(position).getImagePath() == null) {
            loader = Picasso.get().load(imageList.get(position).getImageUrl());
        } else {
            loader = Picasso.get().load(imageList.get(position).getImageUri());
        }

        // Set Picasso options.
        if (scaleType != null && scaleType == ScaleTypes.CENTER_CROP || imageList.get(position).getScaleType() == ScaleTypes.CENTER_CROP) {
            loader.fit().centerCrop();
        } else if (scaleType != null && scaleType == ScaleTypes.CENTER_INSIDE || imageList.get(position).getScaleType() == ScaleTypes.CENTER_INSIDE) {
            loader.fit().centerInside();
        } else if (scaleType != null && scaleType == ScaleTypes.FIT || imageList.get(position).getScaleType() == ScaleTypes.FIT) {
            loader.fit();
        }

        loader.transform(new RoundedTransformation(radius, 0, RoundedTransformation.CornerType.ALL))
                .placeholder(placeholder)
                .error(errorImage)
                .into(imageView);

        container.addView(itemView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastTouchTime = currentTouchTime;
                currentTouchTime = System.currentTimeMillis();
                if (currentTouchTime - lastTouchTime < 250) {
                    if (itemClickListener != null) {
                        itemClickListener.doubleClick(position);
                    }
                } else {
                    if (itemClickListener != null) {
                        itemClickListener.onItemSelected(position);
                    }
                }
            }
        });

        if (touchListener != null) {
            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            touchListener.onTouched(ActionTypes.MOVE, position);
                            break;
                        case MotionEvent.ACTION_DOWN:
                            touchListener.onTouched(ActionTypes.DOWN, position);
                            break;
                        case MotionEvent.ACTION_UP:
                            touchListener.onTouched(ActionTypes.UP, position);
                            break;
                    }
                    return false;
                }
            });
        }

        return itemView;
    }

    public int getGravityFromAlign(String textAlign) {
        switch (textAlign) {
            case "RIGHT":
                return Gravity.RIGHT;
            case "CENTER":
                return Gravity.CENTER;
            default:
                return Gravity.LEFT;
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setTouchListener(TouchListener touchListener) {
        this.touchListener = touchListener;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}