package com.tudoreloprisan.licenta.sdk.model;

/**
 * Created by Doru on 6/25/2016.
 */
public enum SettingType {
    APERTURE("F"),
    FOCUS_MODE("Focus Mode"),
    ISO("ISO"),
    SHUTTER_SPEED("Shutter speed"),
    EXPOSURE_MODES("Exposure Mode");

    private String name;

    public String getName() {
        return name;
    }

    SettingType(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return this.getName();
    }
}
