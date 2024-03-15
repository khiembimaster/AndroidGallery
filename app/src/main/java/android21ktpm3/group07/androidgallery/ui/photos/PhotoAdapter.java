package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.snackbar.Snackbar;

import android21ktpm3.group07.androidgallery.R;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private int[] imageSrcTestData;

    public PhotoAdapter(int[] innerData) {
        this.imageSrcTestData = innerData;
    }
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
                Intent intent = new Intent(v.getContext(), ImageActivity.class);

                intent.putExtra("selected_image", imageSrcTestData[position]); // Truyền ID của ảnh được chọn

                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageSrcTestData.length;
    }
}
