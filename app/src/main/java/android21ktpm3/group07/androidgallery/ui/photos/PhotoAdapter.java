package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors;

import android21ktpm3.group07.androidgallery.BR;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.ImageContainerBinding;
import android21ktpm3.group07.androidgallery.models.Photo;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private final Context context;
    private final List<Photo> photos;

    private ItemActionCallback actionCallback;
    private SelectingModeCallback selectingModeCallback;


    // TODO Separate the selected state to reuse the observable list
    public PhotoAdapter(Context context, ObservableList<Photo> photos) {
        this.context = context;
        this.photos = photos;

        photos.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<Photo>>() {
            @Override
            public void onChanged(ObservableList<Photo> sender) {
                // notifyDataSetChanged();
                // throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void onItemRangeChanged(ObservableList<Photo> sender, int positionStart,
                                           int itemCount) {

                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(ObservableList<Photo> sender, int positionStart,
                                            int itemCount) {
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(ObservableList<Photo> sender, int fromPosition,
                                         int toPosition, int itemCount) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onItemRangeRemoved(ObservableList<Photo> sender, int positionStart,
                                           int itemCount) {
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    public void setItemActionCallback(ItemActionCallback actionCallback) {
        this.actionCallback = actionCallback;
    }

    public void setIsInSelectingModeCallback(SelectingModeCallback selectingModeCallback) {
        this.selectingModeCallback = selectingModeCallback;
    }

    public List<Photo> getSelectedPhoto() {
        return photos.stream().filter(Photo::isSelected).collect(Collectors.toList());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ImageContainerBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.image_container, parent, false
        );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: change this later to use other type of resources
        Photo photo = photos.get(position);

        holder.bind(photo);

        holder.binding.imageView.setOnClickListener(view -> onItemClick(holder, view, photo));
        holder.binding.imageView.setOnLongClickListener(view -> {
            onItemLongClick(holder, view, photo);
            return true;
        });

        // TODO refactor this
        // FIXME scroll not to far and
        if (photo.isSelected()) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_down);
            animation.setFillEnabled(true);
            animation.setFillAfter(true);
            animation.setDuration(0);
            holder.binding.imageView.startAnimation(animation);
        }


        // Glide.with(context)
        //         .load(photo.getPath())
        //         .sizeMultiplier(0.5f)
        //         // .override(100, 100)
        //         .centerCrop()
        //         .placeholder(R.drawable.image_fill1_wght500_grad200_opsz24)
        //         .into(holder.imageView);
        //
        // // TODO: extend onLongClick to turn into selection mode that allows to choose more image
        // //  and replace bottom navbar with a bottom sheet that contains images management
        // //  features(remove, create collection);
        // holder.imageView.setOnLongClickListener(view -> {
        //     if (!holder.isSelected) {
        //         view.startAnimation(holder.scaleDown);
        //         holder.selectedIcon.setVisibility(View.VISIBLE);
        //         holder.isSelected = true;
        //
        //         if (selectedCB != null) {
        //             selectedCB.onItemSelected(photo);
        //         }
        //     }
        //
        //     return true;
        // });
        //
        // // TODO: extend onClick to open image in detail or editor tool
        // holder.imageView.setOnClickListener(view -> {
        //     if (holder.isSelected) {
        //         holder.selectedIcon.setVisibility(View.GONE);
        //         view.startAnimation(holder.scaleUp);
        //         holder.isSelected = false;
        //         if (unselectedCB != null) {
        //             unselectedCB.onItemUnselected(photo);
        //         }
        //     } else {
        //         if (viewCB != null) {
        //             viewCB.onItemView(photo);
        //         }
        //     }
        // });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    private void onItemClick(ViewHolder holder, View view, Photo photo) {
        if (selectingModeCallback.isInSelectingMode()) {
            if (photo.isSelected()) {
                photo.setSelected(false);
                view.startAnimation(holder.scaleUp);
                selectingModeCallback.onRemoveItem();

                if (actionCallback != null) {
                    actionCallback.onItemUnselect(photo);
                }
            } else {
                photo.setSelected(true);
                view.startAnimation(holder.scaleDown);
                selectingModeCallback.onAddItem();

                if (actionCallback != null) {
                    actionCallback.onItemSelect(photo);
                }
            }

            return;
        }

        if (actionCallback != null) {
            actionCallback.onItemView(photo);
        }
    }

    private void onItemLongClick(ViewHolder holder, View view, Photo photo) {
        if (!selectingModeCallback.isInSelectingMode()) {
            photo.setSelected(true);
            view.startAnimation(holder.scaleDown);
            selectingModeCallback.onAddItem();

            if (actionCallback != null) {
                actionCallback.onItemSelect(photo);
            }
        }
    }

    public interface ItemActionCallback {
        void onItemSelect(Photo photo);

        void onItemUnselect(Photo photo);

        void onItemView(Photo photo);
    }

    public interface SelectingModeCallback {
        boolean isInSelectingMode();

        void onAddItem();

        void onRemoveItem();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final Animation scaleDown;
        private final Animation scaleUp;

        private final ImageContainerBinding binding;

        public ViewHolder(ImageContainerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            scaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down);
            scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up);
            scaleDown.setFillEnabled(true);
            scaleDown.setFillAfter(true);
            scaleUp.setFillEnabled(true);
            scaleUp.setFillAfter(true);
        }

        public void bind(Photo photo) {
            binding.setVariable(BR.photo, photo);
            binding.executePendingBindings();
        }
    }
}
