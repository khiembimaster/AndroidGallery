package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.interfaces;


public interface ItemClickListener {
    /**
     * Click listener selected item function.
     *
     * @param position selected item position
     */
    void onItemSelected(int position);

    /**
     * Click listener double click item function.
     *
     * @param position selected item position
     */
    void doubleClick(int position);
}
