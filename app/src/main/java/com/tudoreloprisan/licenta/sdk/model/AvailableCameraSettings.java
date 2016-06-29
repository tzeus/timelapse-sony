package com.tudoreloprisan.licenta.sdk.model;

import java.util.List;

/**
 * Created by Doru on 6/25/2016.
 */
public class AvailableCameraSettings {
    private SettingType setting;
    private String currentSetting;
    private List<String> availableSettings;

    public AvailableCameraSettings() {
    }

    public List<String> getAvailableSettings() {
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

    public void setSetting(SettingType setting) {
        this.setting = setting;
    }

    public SettingType getSetting() {
        return setting;
    }
}
