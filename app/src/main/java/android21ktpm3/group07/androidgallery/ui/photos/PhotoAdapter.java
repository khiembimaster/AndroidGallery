package android21ktpm3.group07.androidgallery.ui.photos;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import android21ktpm3.group07.androidgallery.R;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private ArrayList<Image> imageSrcTestData;
    private Context context;
    private PhotosFragment fragment;


    public static final int REQUEST_IMAGE_ACTIVITY = 1;
   // private OnActivityResultListener onActivityResultListener;

    public PhotoAdapter(Context context,PhotosFragment fragment, ArrayList<Image> innerData) {
        this.context = context;
        this.fragment = fragment;

        this.imageSrcTestData = innerData;
    }
    public PhotoAdapter(Context context,PhotosFragment fragment) {
        this.context = context;
        this.fragment = fragment;

    }
    public PhotoAdapter(Context context, ArrayList<Image> innerData) {
        this.context = context;

        this.imageSrcTestData = innerData;
    }

//    public PhotoAdapter(Context context,ArrayList<Image> innerData, OnActivityResultListener listener) {
//        this.context = context;
//        this.imageSrcTestData = innerData; // Gọi constructor mà không có listener
//        this.onActivityResultListener = listener;
//    }

//    public interface OnActivityResultListener {
//        void onActivityResult(int requestCode, int resultCode, Intent data);
//    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ImageView selectedIcon;
        Animation scaleDown;

        Animation scaleUp;
        Boolean isSelected = false;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            selectedIcon = itemView.findViewById(R.id.selectedIcon);
            scaleDown = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_down);
            scaleUp = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_up);
            scaleDown.setFillEnabled(true);
            scaleDown.setFillAfter(true);
            scaleUp.setFillEnabled(true);
            scaleUp.setFillAfter(true);
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_container, parent, false);
        return new ViewHolder(v);
    }




    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // TODO: change this later to use other type of resources
        holder.imageView.setImageResource(imageSrcTestData.get(position).getImage());
        ViewGroup.LayoutParams lp = holder.imageView.getLayoutParams();

        if (lp instanceof FlexboxLayoutManager.LayoutParams ) {
            FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
            // TODO: Config item attributes here
//            flexboxLp.setFlexGrow(1.0f);
//            flexboxLp.setFlexShrink(1.0f);
//            flexboxLp.setAlignSelf(AlignItems.FLEX_END);
        }


        // TODO: extend onLongClick to turn into selection mode that allows to choose more image
        //  and replace bottom navbar with a bottom sheet that contains images management features(remove, create collection);
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Snackbar.make(v, "A Long Click detected on item ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
                if(!holder.isSelected) {
                    v.startAnimation(holder.scaleDown);
                    holder.selectedIcon.setVisibility(View.VISIBLE);
                    holder.isSelected = true;
                }

                return true;
            }
        });
        // TODO: extend onClick to open image in detail or editor tool
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Snackbar.make(v, "A Click detected on item ", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null)
//                        .show();
//                if(holder.isSelected) {
//                    holder.selectedIcon.setVisibility(View.GONE);
//                    v.startAnimation(holder.scaleUp);
//                    holder.isSelected = false;
//                }
                Intent intent = new Intent(context, ImageActivity.class);

                intent.putExtra("selected_image", imageSrcTestData.get(position));
                fragment.startImageActivity(intent);

                           }
        });

    }

    @Override
    public int getItemCount() {
        return imageSrcTestData.size();
    }


//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        System.out.println("check");
//
//        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_ACTIVITY) {
//            if (data != null && data.hasExtra("updated_comment")) {
//                String updatedComment = data.getStringExtra("updated_comment");
//                notifyDataSetChanged(); // Notify the adapter that the data has changed
//            }
//        }
//    }




}
