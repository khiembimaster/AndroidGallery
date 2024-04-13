package android21ktpm3.group07.androidgallery.ui.memories;


import android.app.Activity;
import com.hw.photomovie.render.GLTextureView;


import java.util.List;

import android21ktpm3.group07.androidgallery.ui.memories.widget.FilterItem;
import android21ktpm3.group07.androidgallery.ui.memories.widget.TransferItem;

/**
 * Created by huangwei on 2018/9/9.
 */
public interface IDemoView {
    public GLTextureView getGLView();
    void setFilters(List<FilterItem> filters);
    Activity getActivity();

    void setTransfers(List<TransferItem> items);
}