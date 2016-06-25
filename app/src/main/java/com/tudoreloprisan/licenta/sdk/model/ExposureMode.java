package com.tudoreloprisan.licenta.sdk.model;

/**
 * Created by Doru on 6/25/2016.
 */
public enum ExposureMode {
    MANUAL("Manual"),
    PROGRAM_AUTO("Auto"),
    APERTURE("Aperture"),
    SHUTTER("Shutter"),
    INTELLIGENT_AUTO("Intelligent Auto"),
    SUPERIOR_AUTO("Superior Auto");


    private String name;


    ExposureMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
