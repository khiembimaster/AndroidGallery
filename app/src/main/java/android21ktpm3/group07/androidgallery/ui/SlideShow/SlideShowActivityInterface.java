package android21ktpm3.group07.androidgallery.ui.SlideShow;

import android.content.Context;
import android.view.View;

import android21ktpm3.group07.androidgallery.SlideShowCustom.ImageSlider;

public interface SlideShowActivityInterface {
    void openImageChooser();
    void openAudioPicker();


    void ResultchooseImages(ImageSlider imageSlider);

    void chooseAudio(ImageSlider imageSlider);

    void showPopupMenu(ImageSlider imageSlider, View view);


    void handlePermission(Context context);

    void showPopupMenu(View view, View v);
}
