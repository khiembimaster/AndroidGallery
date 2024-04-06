package android21ktpm3.group07.androidgallery.SlideShowCustom.constants;



public enum AnimationTypes {
    ZOOM_IN("ZoomIn"),
    ZOOM_OUT("ZoomOut"),
    DEPTH_SLIDE("DepthSlide"),
    CUBE_IN("CubeIn"),
    CUBE_OUT("CubeOut"),
    FLIP_HORIZONTAL("FlipHorizontal"),
    FLIP_VERTICAL("FlipVertical"),
    FOREGROUND_TO_BACKGROUND("ForegroundToBackground"),
    BACKGROUND_TO_FOREGROUND("BackgroundToForeground"),
    ROTATE_UP("RotateUp"),
    ROTATE_DOWN("Rotate_Down"),
    GATE("Gate"),
    TOSS("Toss"),
    FIDGET_SPINNER("FidgetSpinner");

    private final String value;

    AnimationTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

