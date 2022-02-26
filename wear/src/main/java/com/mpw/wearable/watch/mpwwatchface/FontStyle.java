package com.mpw.wearable.watch.mpwwatchface;

/**
 * Created by malith on 6/16/18.
 */

public class FontStyle {
    private String style;
    private int textSize;

    public FontStyle(String style, int textSize) {
        this.style = style;
        this.textSize = textSize;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }
}
