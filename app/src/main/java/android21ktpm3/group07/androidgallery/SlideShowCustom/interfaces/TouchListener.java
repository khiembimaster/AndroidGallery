package android21ktpm3.group07.androidgallery.SlideShowCustom.interfaces;


import android21ktpm3.group07.androidgallery.SlideShowCustom.constants.ActionTypes;

public interface TouchListener {
    /**
     * Click listener touched item function.
     *
     * @param touched  slider boolean
     */
    void onTouched(ActionTypes touched, int position);

}