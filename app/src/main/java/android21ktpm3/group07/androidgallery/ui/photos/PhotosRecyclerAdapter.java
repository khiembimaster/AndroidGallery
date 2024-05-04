package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import android21ktpm3.group07.androidgallery.BR;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.ImagesByDateLayoutBinding;
import android21ktpm3.group07.androidgallery.helpers.DateHelper;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.models.PhotoGroup;

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {
    private final Context context;
    private final List<Item> items;

    private int imagesPerRow = 4;
    private int selectedCount = 0;
    private PhotoAdapter.ItemActionCallback childItemActionCallback;
    private final PhotoAdapter.SelectingModeCallback selectingModeCallback;
    private SelectingModeDisplayingCallback selectingModeDisplayingCallback;

    public PhotosRecyclerAdapter(Context context, ObservableList<PhotoGroup> photoGroups) {
        this.context = context;
        this.items = photoGroups.stream()
                .map(Item::new)
                .collect(Collectors.toList());

        selectingModeCallback = new PhotoAdapter.SelectingModeCallback() {
            @Override
            public boolean isInSelectingMode() {
                return selectedCount > 0;
            }

            @Override
            public void onAddItem() {
                selectedCount++;

                if (selectedCount == 1 && selectingModeDisplayingCallback != null) {
                    selectingModeDisplayingCallback.onEnter();
                }
            }

            @Override
            public void onRemoveItem() {
                selectedCount--;

                if (selectedCount == 0 && selectingModeDisplayingCallback != null) {
                    selectingModeDisplayingCallback.onExit();
                }
            }

            @Override
            public void onClearAllItems(int num) {
                selectedCount -= num;

                if (selectedCount == 0 && selectingModeDisplayingCallback != null) {
                    selectingModeDisplayingCallback.onExit();
                }
            }
        };


        // TODO: Should we separate adapter to a different list?
        photoGroups.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<PhotoGroup>>() {
            @Override
            public void onChanged(ObservableList<PhotoGroup> sender) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void onItemRangeChanged(
                    ObservableList<PhotoGroup> sender, int positionStart, int itemCount
            ) {
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(ObservableList<PhotoGroup> sender, int positionStart,
                                            int itemCount) {
                items.addAll(
                        positionStart,
                        sender.subList(positionStart, positionStart + itemCount)
                                .stream()
                                .map(Item::new)
                                .collect(Collectors.toList())
                );

                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(ObservableList<PhotoGroup> sender, int fromPosition,
                                         int toPosition, int itemCount) {
                Collections.swap(items, fromPosition, toPosition);
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onItemRangeRemoved(ObservableList<PhotoGroup> sender, int positionStart,
                                           int itemCount) {
                // TODO Check if there is any memory leak
                items.subList(positionStart, positionStart + itemCount).clear();

                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    public void setChildItemActionCallback(PhotoAdapter.ItemActionCallback callback) {
        this.childItemActionCallback = callback;
    }

    public void setSelectingModeDisplayingCallback(SelectingModeDisplayingCallback callback) {
        this.selectingModeDisplayingCallback = callback;
    }

    public List<Photo> getSelectedPhotos() {
        return items.stream()
                .map(item -> {
                    if (item.adapter != null) {
                        return item.adapter.getSelectedPhoto();
                    }
                    return Collections.<Photo>emptyList();
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public void clearSelectedPhotos() {
        items.forEach(item -> {
            if (item.adapter != null) {
                item.adapter.clearSelectedPhoto();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ImagesByDateLayoutBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.images_by_date_layout, parent, false
        );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item cur = items.get(position);

        holder.bind(cur.photoGroup);

        if (cur.adapter == null) {
            PhotoAdapter adapter = new PhotoAdapter(context, cur.photoGroup.getPhotos());
            adapter.setIsInSelectingModeCallback(selectingModeCallback);
            adapter.setItemActionCallback(childItemActionCallback);
            cur.adapter = adapter;
        }

        holder.binding.imageByDateRecyclerView.setAdapter(cur.adapter);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.binding.imageByDateRecyclerView.setAdapter(null);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @BindingAdapter("android:text")
    public static void setLocalDate(TextView tv, LocalDate date) {
        tv.setText(DateHelper.formatDate(date, "dd/MM/yyyy"));
    }

    public interface SelectingModeDisplayingCallback {
        void onExit();

        void onEnter();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImagesByDateLayoutBinding binding;

        public ViewHolder(@NonNull ImagesByDateLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Flexbox won't display all images: https://github.com/google/flexbox-layout/issues/420
            // FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
            // layoutManager.setFlexDirection(FlexDirection.ROW);

            GridLayoutManager layoutManager = new GridLayoutManager(context, imagesPerRow);
            binding.imageByDateRecyclerView.setLayoutManager(layoutManager);
        }

        public void bind(PhotoGroup photoGroup) {
            binding.setVariable(BR.photoGroup, photoGroup);
            binding.executePendingBindings();
        }
    }

    private static class Item {
        PhotoAdapter adapter;
        PhotoGroup photoGroup;

        Item(PhotoGroup photoGroup) {
            this.photoGroup = photoGroup;
        }
    }
}
