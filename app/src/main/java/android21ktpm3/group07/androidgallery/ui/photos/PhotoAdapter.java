package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Photo;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private final Context context;
    private final List<Photo> photos;

    public PhotoAdapter(Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageView selectedIcon;
        private final Animation scaleDown;
        private final Animation scaleUp;
        Boolean isSelected = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            selectedIcon = itemView.findViewById(R.id.selectedIcon);


            scaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down);
            scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up);
            scaleDown.setFillEnabled(true);
            scaleDown.setFillAfter(true);
            scaleUp.setFillEnabled(true);
            scaleUp.setFillAfter(true);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.image_container, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: change this later to use other type of resources
        Photo photo = photos.get(position);

        Glide.with(context)
                .load(photo.getPath())
                .sizeMultiplier(0.5f)
                // .override(100, 100)
                .centerCrop()
                .placeholder(R.drawable.image_fill1_wght500_grad200_opsz24)
                .into(holder.imageView);
        
        // TODO: extend onLongClick to turn into selection mode that allows to choose more image
        //  and replace bottom navbar with a bottom sheet that contains images management
        //  features(remove, create collection);
        holder.imageView.setOnLongClickListener(view -> {
            if (!holder.isSelected) {
                view.startAnimation(holder.scaleDown);
                holder.selectedIcon.setVisibility(View.VISIBLE);
                holder.isSelected = true;

                if (selectedCB != null) {
                    selectedCB.onItemSelected(photo);
                }
            }

            return true;
        });

        // TODO: extend onClick to open image in detail or editor tool
        holder.imageView.setOnClickListener(view -> {
            if (holder.isSelected) {
                holder.selectedIcon.setVisibility(View.GONE);
                view.startAnimation(holder.scaleUp);
                holder.isSelected = false;
                if (unselectedCB != null) {
                    unselectedCB.onItemUnselected(photo);
                }
            } else {
                if (viewCB != null) {
                    viewCB.onItemView(photo);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return photos.size();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Photo photo);
    }

    public interface OnItemUnselectedListener {
        void onItemUnselected(Photo photo);
    }

    public interface OnItemViewListener {
        void onItemView(Photo photo);
    }

    @Nullable
    public OnItemSelectedListener selectedCB;
    @Nullable
    public OnItemUnselectedListener unselectedCB;
    @Nullable
    public OnItemViewListener viewCB;

    public void setOnItemSelectedListener(OnItemSelectedListener cb) {
        selectedCB = cb;
    }

    public void setOnItemUnselectedListener(OnItemUnselectedListener cb) {
        unselectedCB = cb;
    }

    public void setOnItemViewListener(OnItemViewListener cb) {
        viewCB = cb;
    }
}
