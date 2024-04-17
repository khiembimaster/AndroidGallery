package android21ktpm3.group07.androidgallery.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import android21ktpm3.group07.androidgallery.BR;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.SearchImageItemBinding;


public class SearchResultRVAdapter extends RecyclerView.Adapter<SearchResultRVAdapter.ViewHolder>{

    private Context context;
    private List<Photo> photos;

    public SearchResultRVAdapter(android.content.Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SearchImageItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.search_image_item ,parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Photo photo = photos.get(position);
        holder.bind(photo);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private SearchImageItemBinding binding;

        public ViewHolder(SearchImageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Object object) {
            binding.setVariable(BR.photo, object);
            binding.executePendingBindings();
            Photo photo = (Photo) object;
            List<String> tags = photo.getTags();
            if(tags != null && tags.size() > 0){
                ChipGroup chipGroup = binding.chipGroupTags;
                for(String tag : photo.getTags()){
                    Chip chip = new Chip(context);
                    chip.setText(tag);
                    chipGroup.addView(chip);
                }
            }
        }
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

}
