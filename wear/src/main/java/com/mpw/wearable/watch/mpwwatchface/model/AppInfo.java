package com.mpw.wearable.watch.mpwwatchface.model;

import android.graphics.drawable.Drawable;

/**
 * Created by malith on 6/17/18.
 */

public class AppInfo {
    private CharSequence label;
    private CharSequence packageName;
    private Drawable icon;

    public CharSequence getLabel() {
        return label;
    }

    public void setLabel(CharSequence label) {
        this.label = label;
    }

    public CharSequence getPackageName() {
        return packageName;
    }

    public void setPackageName(CharSequence packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

}
