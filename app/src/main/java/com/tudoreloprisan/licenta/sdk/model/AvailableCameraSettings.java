package com.tudoreloprisan.licenta.sdk.model;

import java.util.List;

/**
 * Created by Doru on 6/25/2016.
 */
public class AvailableCameraSettings {
    private SettingType setting;
    private String currentSetting;
    private List<String> availableSettings;

    public List<String> getAvailableSettings() {
        if (availableSettings== null){
            return null;
        }
            return availableSettings;
    }

    public void setAvailableSettings(List<String> availableSettings) {

        this.availableSettings = availableSettings;
    }

    public String getCurrentSetting() {
        return currentSetting;
    }

    public void setCurrentSetting(String currentSetting) {
        this.currentSetting = currentSetting;
    }

    public AvailableCameraSettings(SettingType setting) {
        this.setting = setting;
    }
}
