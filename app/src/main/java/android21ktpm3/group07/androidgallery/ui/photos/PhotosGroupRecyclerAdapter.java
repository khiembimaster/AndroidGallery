package android21ktpm3.group07.androidgallery.ui.photos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.AlignSelf;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.snackbar.Snackbar;

import android21ktpm3.group07.androidgallery.R;

public class PhotosGroupRecyclerAdapter extends RecyclerView.Adapter<PhotosGroupRecyclerAdapter.ViewHolder> {
    private int[] imageSrcTestData;

    public PhotosGroupRecyclerAdapter(int[] innerData) {
        this.imageSrcTestData = innerData;
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);

            // TODO: extend onLongClick to turn into selection mode that allows to choose more image
            //  and replace bottom navbar with a bottom sheet that contains images management features(remove, create collection);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    Snackbar.make(v, "A Long Click detected on item " + (position+1), Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show();
                    return true;
                }
            });
            // TODO: extend onClick to open image in detail or editor tool
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Snackbar.make(v, "A Click detected on item " + (position+1), Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show();
                }
            });

        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_container, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: change this later to use other type of resources
        holder.imageView.setImageResource(imageSrcTestData[position]);
        ViewGroup.LayoutParams lp = holder.imageView.getLayoutParams();


        if (lp instanceof FlexboxLayoutManager.LayoutParams ) {
            FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
            // TODO: Config item attributes here
//            flexboxLp.setFlexGrow(1.0f);
//            flexboxLp.setFlexShrink(1.0f);
//            flexboxLp.setAlignSelf(AlignItems.FLEX_END);
        }
    }

    @Override
    public int getItemCount() {
        return imageSrcTestData.length;
    }
}
