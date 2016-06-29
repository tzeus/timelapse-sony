package com.tudoreloprisan.licenta.timelapse;

import com.tudoreloprisan.licenta.sdk.model.AvailableCameraSettings;
import com.tudoreloprisan.licenta.sdk.model.SettingType;

/**
 * Created by tudorel.oprisan on 6/29/2016.
 */
public class CameraSettingsUtil {
    private SettingType settingType;
    private AvailableCameraSettings cameraSettings;



    public CameraSettingsUtil(AvailableCameraSettings cameraSettings) {
        this.cameraSettings = cameraSettings;
    }

    public AvailableCameraSettings getCameraSettings() {
        return cameraSettings;
    }

    public void setCameraSettings(AvailableCameraSettings cameraSettings) {
        this.cameraSettings = cameraSettings;
    }

    public SettingType getSettingType() {
        return settingType;
    }

    public void setSettingType(SettingType settingType) {
        this.settingType = settingType;
    }
}
