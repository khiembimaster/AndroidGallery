package android21ktpm3.group07.androidgallery.SlideShowCustom.constants;

public enum ActionTypes {
    DOWN("down"),
    UP("up"),
    MOVE("move");

    private final String value;

    ActionTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

